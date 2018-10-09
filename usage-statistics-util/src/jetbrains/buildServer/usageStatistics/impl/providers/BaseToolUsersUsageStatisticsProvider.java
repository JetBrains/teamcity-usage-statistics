/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.usageStatistics.impl.providers;

import gnu.trove.TLongHashSet;
import gnu.trove.TLongLongHashMap;
import gnu.trove.TLongLongIterator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.usageStatistics.util.BaseUsageStatisticsStatePersister;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TimeService;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseToolUsersUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {

  @NotNull
  private final Map<String, TLongLongHashMap> myToolUsages = new ConcurrentHashMap<>();

  @NotNull
  private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

  @NotNull
  private final TimeService myTimeService;

  @SuppressWarnings("WeakerAccess")
  public BaseToolUsersUsageStatisticsProvider(@NotNull final SBuildServer server,
                                              @NotNull final ServerPaths serverPaths,
                                              @NotNull final LinkedHashMap<Long, String> periodDescriptions,
                                              @NotNull final TimeService timeService) {
    super(periodDescriptions, new PercentageFormatter(1).format(0));
    myTimeService = timeService;
    registerPersistor(server, serverPaths);
  }

  @NotNull
  protected abstract String getExternalId();

  @NotNull
  protected abstract String getToolName();

  @NotNull
  protected abstract String getToolIdName();

  protected abstract boolean publishToolUsages(@NotNull String toolId);

  @NotNull
  protected abstract String getValueTooltip();

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    removeObsolete();
    final Map<String, TLongLongHashMap> filtered = filter(startDate);
    final UsageStatisticsFormatter formatter = new PercentageFormatter(getTotalUsersCount(startDate));
    filtered.keySet().stream()
            .filter(this::publishToolUsages)
            .sorted()
            .forEach(toolIdSource -> {
              final String statisticId = makeId(periodDescription, toolIdSource);
              presentationManager.applyPresentation(statisticId, toolIdSource, myGroupName, formatter, getValueTooltip());
              publisher.publishStatistic(statisticId, filtered.get(toolIdSource).size());
            });
  }

  @Override
  protected boolean mustSortStatistics() {
    return true;
  }

  protected void addUsage(@NotNull final String toolIdSource, final long userId) {
    myLock.readLock().lock();
    try {
      myToolUsages.computeIfAbsent(toolIdSource, k -> new TLongLongHashMap())
                  .put(userId, myTimeService.now());
    } finally {
      myLock.readLock().unlock();
    }
  }

  @NotNull
  Set<String> getUsers(final long fromTimestamp) {
    final Set<String> result = new HashSet<>();
    filter(fromTimestamp).values().forEach(map -> {
      map.forEachKey(userId -> {
        result.add(String.valueOf(userId));
        return true;
      });
    });
    return result;
  }

  protected int getTotalUsersCount(final long startDate) {
    final TLongHashSet userIds = new TLongHashSet();
    myToolUsages.values().forEach(map -> {
      map.forEachEntry((userId, timestamp) -> {
        if (timestamp > startDate) {
          userIds.add(userId);
        }
        return true;
      });
    });
    return userIds.size();
  }

  private void removeObsolete() {
    myLock.writeLock().lock();
    try {
      final long date = getThresholdDate();
      myToolUsages.keySet().forEach(key -> {
        myToolUsages.compute(key, (k, existingMap) -> {
          if (existingMap == null || existingMap.isEmpty()) {
            return existingMap;
          }
          final TLongLongIterator iter = existingMap.iterator();
          while (iter.hasNext()) {
            iter.advance();
            long timestamp = iter.value();
            if (timestamp < date) {
              iter.remove();
            }
          }
          return existingMap;
        });
      });
    } finally {
      myLock.writeLock().unlock();
    }
  }

  private Map<String, TLongLongHashMap> filter(final long startDate) {
    final Map<String, TLongLongHashMap> result = new HashMap<>();
    myToolUsages.forEach((id, map) -> {
      final TLongLongHashMap filtered = filter(map, startDate);
      if (!filtered.isEmpty()) {
        result.put(id, filtered);
      }
    });
    return result;
  }

  private TLongLongHashMap filter(TLongLongHashMap map, long threshold) {
    final TLongLongHashMap result = new TLongLongHashMap();
    map.forEachEntry((key, value) -> {
      if (value > threshold) {
        result.put(key, value);
      }
      return true;
    });
    return result;
  }

  @NonNls @NotNull private static final String USAGE = "usage";
  @NonNls @NotNull private static final String USER_ID = "userId";
  @NonNls @NotNull private static final String TIMESTAMP = "timestamp";

  private void writeExternal(@NotNull final Element element) {
    myLock.writeLock().lock();
    try {
      final long threshold = getThresholdDate();
      myToolUsages.forEach((id, usageMap) -> {
        final Element toolElement = new Element(getToolName());
        toolElement.setAttribute(getToolIdName(), StringUtil.replaceInvalidXmlChars(id));
        element.addContent((Content)toolElement);
        usageMap.forEachEntry((userId, timestamp) -> {
          if (timestamp > threshold) {
            final Element usageElement = new Element(USAGE);
            usageElement.setAttribute(USER_ID, String.valueOf(userId));
            usageElement.setAttribute(TIMESTAMP, String.valueOf(timestamp));
            toolElement.addContent((Content)usageElement);
          }
          return true;
        });
      });
    } finally {
      myLock.writeLock().unlock();
    }
  }

  private void readExternal(@NotNull final Element element) {
    myLock.writeLock().lock();
    try {
      final long threshold = getThresholdDate();
      myToolUsages.clear();
      for (final Object tool : element.getChildren(getToolName())) {
        if (!(tool instanceof Element)) continue;
        final Element toolElement = (Element)tool;
        final String toolIdSource = toolElement.getAttributeValue(getToolIdName());
        if (toolIdSource == null) continue;
        final TLongLongHashMap value = new TLongLongHashMap();
        myToolUsages.put(toolIdSource, value);
        for (final Object usage : toolElement.getChildren(USAGE)) {
          if (!(usage instanceof Element)) continue;
          final Element usageElement = (Element)usage;
          final String userIdStr = usageElement.getAttributeValue(USER_ID);
          if (userIdStr == null) continue;
          final String timestampStr = usageElement.getAttributeValue(TIMESTAMP);
          if (timestampStr == null) continue;
          try {
            long timestamp = Long.parseLong(timestampStr);
            if (timestamp > threshold) {
              long userId = Long.parseLong(userIdStr);
              value.put(userId, timestamp);
            }
          } catch (final NumberFormatException ignored) {
          }
        }
      }
    } finally {
      myLock.writeLock().unlock();
    }
  }

  private void registerPersistor(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    new BaseUsageStatisticsStatePersister(server, serverPaths) {
      @NotNull
      @Override
      protected String getStateName() {
        return getExternalId();
      }

      @Override
      protected void writeExternal(@NotNull final Element element) {
        BaseToolUsersUsageStatisticsProvider.this.writeExternal(element);
      }

      @Override
      protected void readExternal(@NotNull final Element element) {
        BaseToolUsersUsageStatisticsProvider.this.readExternal(element);
      }
    };
  }
}
