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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.usageStatistics.util.BaseUsageStatisticsStatePersister;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TimeService;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseToolUsersUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {

  @NotNull
  private final Map<String, Map<Long, Long>> myToolUsages = new ConcurrentHashMap<>();

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
    final Map<String, Map<Long, Long>> filtered = filter(startDate);
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
      myToolUsages.computeIfAbsent(toolIdSource, k -> new ConcurrentHashMap<>())
                  .put(userId, myTimeService.now());
    } finally {
      myLock.readLock().unlock();
    }
  }


  @SuppressWarnings("WeakerAccess")
  @NotNull
  public Set<String> getUsers(final long fromTimestamp) {
    return filter(fromTimestamp).values()
                                .stream()
                                .map(it -> it.keySet())
                                .flatMap(it -> it.stream())
                                .map(it -> it.toString())
                                .collect(Collectors.toSet());
  }

  protected int getTotalUsersCount(final long startDate) {
    final Set<Long> userIds = new HashSet<>();
    myToolUsages.values().stream().map(Map::values).forEach(userIds::addAll);
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
          existingMap.entrySet().removeIf(e -> e.getValue() < date);
          return existingMap;
        });
      });
    } finally {
      myLock.writeLock().unlock();
    }
  }

  private Map<String, Map<Long, Long>> filter(final long startDate) {
    final Map<String, Map<Long, Long>> result = new HashMap<>();
    myToolUsages.forEach((id, map) -> {
      final Map<Long, Long> filtered = filter(map, startDate);
      if (!filtered.isEmpty()) {
        result.put(id, filtered);
      }
    });
    return result;
  }

  private Map<Long, Long> filter(Map<Long, Long> map, long threshold) {
    return CollectionsUtil.filterMapByValues(map, val -> val > threshold);
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
        usageMap.forEach((userId, timestamp) -> {
          if (timestamp > threshold) {
            final Element usageElement = new Element(USAGE);
            usageElement.setAttribute(USER_ID, String.valueOf(userId));
            usageElement.setAttribute(TIMESTAMP, String.valueOf(timestamp));
            toolElement.addContent((Content)usageElement);
          }
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
        final Map<Long, Long> value = new ConcurrentHashMap<>();
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
