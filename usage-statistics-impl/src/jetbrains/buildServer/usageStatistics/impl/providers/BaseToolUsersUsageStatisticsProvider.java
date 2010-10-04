/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.usageStatistics.util.BasePersistentStateComponent;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.filters.Filter;
import jetbrains.buildServer.util.filters.FilterUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

abstract class BaseToolUsersUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private static final Comparator<String> STRINGS_COMPARATOR = new Comparator<String>() {
    public int compare(@NotNull final String s1, @NotNull final String s2) {
      return s1.compareToIgnoreCase(s2);
    }
  };

  @NotNull private final Map<String, Set<ToolUsage>> myToolUsages = new TreeMap<String, Set<ToolUsage>>();

  protected BaseToolUsersUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                 @NotNull final ServerPaths serverPaths,
                                                 @NotNull final UsageStatisticsPresentationManager presentationManager) {
    this(server, serverPaths, presentationManager, createDefaultPeriodDescriptions());
  }

  protected BaseToolUsersUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                 @NotNull final ServerPaths serverPaths,
                                                 @NotNull final UsageStatisticsPresentationManager presentationManager,
                                                 @NotNull final Map<Long, String> periodDescriptions) {
    super(server, presentationManager, periodDescriptions);
    registerPersistor(server, serverPaths);
  }

  @NotNull
  protected abstract String getId();

  @NotNull
  protected abstract String getToolName();

  @NotNull
  protected abstract String getToolIdName();

  @NotNull
  protected abstract String prepareDisplayName(@NotNull String toolId, @NotNull String periodDescription);

  @NotNull
  protected abstract String getGroupName(@NotNull String periodDescription);

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final String periodDescription, final long startDate) {
    removeObsoleteUsages();
    final Map<String, Set<ToolUsage>> usages = filterUsages(startDate);
    final UsageStatisticsFormatter formatter = new PercentageFormatter(getTotalUsagesCount(usages));
    final List<String> toolIds = new ArrayList<String>(usages.keySet());
    Collections.sort(toolIds, STRINGS_COMPARATOR);
    for (final String toolId : toolIds) {
      final String statisticId = "jetbrains.buildServer.usageStatistics." + getId() + "[" + toolId.replace(' ', '.') + "].ForTheLast" + periodDescription;
      myPresentationManager.applyPresentation(statisticId, prepareDisplayName(toolId, periodDescription), getGroupName(periodDescription), formatter);
      publisher.publishStatistic(statisticId, usages.get(toolId).size());
    }
  }

  protected synchronized void addUsage(@NotNull final String toolId, final long userId) {
    if (!myToolUsages.containsKey(toolId)) {
      myToolUsages.put(toolId, new HashSet<ToolUsage>());
    }
    final Set<ToolUsage> toolUsages = myToolUsages.get(toolId);
    final ToolUsage usage = new ToolUsage(String.valueOf(userId), Dates.now().getTime());
    toolUsages.remove(usage);
    toolUsages.add(usage);
  }

  private synchronized void removeObsoleteUsages() {
    final Filter<ToolUsage> filter = createDateFilter(getThresholdDate());
    for (final Set<ToolUsage> pageUsages : myToolUsages.values()) {
      FilterUtil.filterCollection(pageUsages, filter);
    }
  }

  @NotNull
  private static Filter<ToolUsage> createDateFilter(final long threshold) {
    return new Filter<ToolUsage>() {
      public boolean accept(@NotNull final ToolUsage usage) {
        return usage.getTimestamp() > threshold;
      }
    };
  }

  private int getTotalUsagesCount(@NotNull final Map<String, Set<ToolUsage>> usages) {
    int totalUsages = 0;
    for (final Set<ToolUsage> toolUsages : usages.values()) {
      totalUsages += toolUsages.size();
    }
    return totalUsages;
  }

  @NotNull
  private synchronized Map<String, Set<ToolUsage>> filterUsages(final long startDate) {
    final Filter<ToolUsage> filter = createDateFilter(startDate);
    final Map<String, Set<ToolUsage>> result = new HashMap<String, Set<ToolUsage>>();
    for (final Map.Entry<String, Set<ToolUsage>> entry : myToolUsages.entrySet()) {
      final Set<ToolUsage> usages = entry.getValue();
      final HashSet<ToolUsage> filteredUsages = FilterUtil.filterAndCopy(usages, new HashSet<ToolUsage>(), filter);
      if (!filteredUsages.isEmpty() || hasActiveUsage(usages)) {
        result.put(entry.getKey(), filteredUsages);
      }
    }
    return result;
  }

  private boolean hasActiveUsage(@NotNull final Set<ToolUsage> usages) {
    final Filter<ToolUsage> filter = createDateFilter(getThresholdDate());
    for (final ToolUsage usage : usages) {
      if (filter.accept(usage)) {
        return true;
      }
    }
    return false;
  }

  @NonNls @NotNull private static final String USAGE = "usage";
  @NonNls @NotNull private static final String USER_ID = "userId";
  @NonNls @NotNull private static final String TIMESTAMP = "timestamp";

  private synchronized void writeExternal(@NotNull final Element element) {
    final Filter<ToolUsage> filter = createDateFilter(getThresholdDate());
    for (final Map.Entry<String, Set<ToolUsage>> entry : myToolUsages.entrySet()) {
      final Element toolElement = new Element(getToolName());
      toolElement.setAttribute(getToolIdName(), entry.getKey());
      element.addContent(toolElement);
      for (final ToolUsage usage : entry.getValue()) {
        if (!filter.accept(usage)) continue;
        final Element usageElement = new Element(USAGE);
        usageElement.setAttribute(USER_ID, usage.getUserId());
        usageElement.setAttribute(TIMESTAMP, String.valueOf(usage.getTimestamp()));
        toolElement.addContent(usageElement);
      }
    }
  }

  private synchronized void readExternal(@NotNull final Element element) {
    final Filter<ToolUsage> filter = createDateFilter(getThresholdDate());
    myToolUsages.clear();
    for (final Object tool : element.getChildren(getToolName())) {
      if (!(tool instanceof Element)) continue;
      final Element toolElement = (Element)tool;
      final String toolId = toolElement.getAttributeValue(getToolIdName());
      if (toolId == null) continue;
      myToolUsages.put(toolId, new HashSet<ToolUsage>());
      for (final Object usage : toolElement.getChildren(USAGE)) {
        if (!(usage instanceof Element)) continue;
        final Element usageElement = (Element)usage;
        final String userId = usageElement.getAttributeValue(USER_ID);
        if (userId == null) continue;
        final String timestampStr = usageElement.getAttributeValue(TIMESTAMP);
        if (timestampStr == null) continue;
        try {
          final ToolUsage toolUsage = new ToolUsage(userId, Long.parseLong(timestampStr));
          if (filter.accept(toolUsage)) {
            myToolUsages.get(toolId).add(toolUsage);
          }
        }
        catch (final NumberFormatException ignore) {}
      }
    }
  }

  private void registerPersistor(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    new BasePersistentStateComponent(server, serverPaths) {
      @NotNull
      @Override
      protected String getId() {
        return BaseToolUsersUsageStatisticsProvider.this.getId();
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

  private static class ToolUsage {
    @NotNull private final String myUserId;
    private final long myTimestamp;

    public ToolUsage(@NotNull final String userId, final long timestamp) {
      myUserId = userId;
      myTimestamp = timestamp;
    }

    @NotNull
    public String getUserId() {
      return myUserId;
    }

    public long getTimestamp() {
      return myTimestamp;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof ToolUsage)) return false;
      return myUserId.equals(((ToolUsage)o).myUserId);
    }

    @Override
    public int hashCode() {
      return myUserId.hashCode();
    }
  }
}
