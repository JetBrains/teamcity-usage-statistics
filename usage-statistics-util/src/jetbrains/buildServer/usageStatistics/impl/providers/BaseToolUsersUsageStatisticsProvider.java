/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
  @NotNull private final Map<ICString, Set<ToolUsage>> myToolUsages = new TreeMap<ICString, Set<ToolUsage>>();

  protected BaseToolUsersUsageStatisticsProvider(@NotNull final SBuildServer server,
                                                 @NotNull final ServerPaths serverPaths,
                                                 @NotNull final LinkedHashMap<Long, String> periodDescriptions) {
    super(periodDescriptions, new PercentageFormatter(1).format(0));
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
    removeObsoleteUsages();
    final Map<ICString, Set<ToolUsage>> usages = filterUsages(startDate);
    patchUsagesIfNeeded(usages);
    final UsageStatisticsFormatter formatter = new PercentageFormatter(getTotalUsersCount(usages, startDate));
    final List<ICString> toolIds = new ArrayList<ICString>(usages.keySet());
    Collections.sort(toolIds);
    for (final ICString toolId : toolIds) {
      final String toolIdSource = toolId.getSource();
      if (!publishToolUsages(toolIdSource)) continue;
      final String statisticId = makeId(periodDescription, toolIdSource);
      presentationManager.applyPresentation(statisticId, toolIdSource, myGroupName, formatter, getValueTooltip());
      publisher.publishStatistic(statisticId, usages.get(toolId).size());
    }
  }

  protected void patchUsagesIfNeeded(@NotNull final Map<ICString, Set<ToolUsage>> usages) {}

  protected int getTotalUsersCount(@NotNull final Map<ICString, Set<ToolUsage>> usages, final long startDate) {
    return getUsers(usages).size();
  }

  @Override
  protected boolean mustSortStatistics() {
    return true;
  }

  protected synchronized void addUsage(@NotNull final String toolIdSource, final long userId) {
    final ICString toolId = new ICString(toolIdSource);
    if (myToolUsages.containsKey(toolId)) {
      updateSourceIfNeeded(toolId);
    } else {
      myToolUsages.put(toolId, new HashSet<ToolUsage>());
    }
    final Set<ToolUsage> toolUsages = myToolUsages.get(toolId);
    final ToolUsage usage = new ToolUsage(String.valueOf(userId), Dates.now().getTime());
    toolUsages.remove(usage);
    toolUsages.add(usage);
  }

  @NotNull
  protected Set<String> getUsers(final long fromTimestamp) {
    return getUsers(filterUsages(fromTimestamp));
  }

  @NotNull
  private static Set<String> getUsers(@NotNull final Map<ICString, Set<ToolUsage>> usages) {
    final Set<String> userIds = new HashSet<String>();
    for (final Set<ToolUsage> toolUsages : usages.values()) {
      for (final ToolUsage toolUsage : toolUsages) {
        userIds.add(toolUsage.getUserId());
      }
    }
    return userIds;
  }

  private void updateSourceIfNeeded(@NotNull final ICString toolId) {
    for (final ICString currentToolId : myToolUsages.keySet()) {
      if (currentToolId.equals(toolId)) {
        if (!publishToolUsages(currentToolId.getSource())) {
          currentToolId.updateSource(toolId.getSource());
        }
        break;
      }
    }
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

  @NotNull
  private synchronized Map<ICString, Set<ToolUsage>> filterUsages(final long startDate) {
    final Filter<ToolUsage> filter = createDateFilter(startDate);
    final Map<ICString, Set<ToolUsage>> result = new HashMap<ICString, Set<ToolUsage>>();
    for (final Map.Entry<ICString, Set<ToolUsage>> entry : myToolUsages.entrySet()) {
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
    for (final Map.Entry<ICString, Set<ToolUsage>> entry : myToolUsages.entrySet()) {
      final Element toolElement = new Element(getToolName());
      toolElement.setAttribute(getToolIdName(), entry.getKey().getSource());
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
      final String toolIdSource = toolElement.getAttributeValue(getToolIdName());
      if (toolIdSource == null) continue;
      final ICString toolId = new ICString(toolIdSource);
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
        return BaseToolUsersUsageStatisticsProvider.this.getExternalId();
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

  protected static class ToolUsage {
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

  protected static class ICString implements Comparable<ICString> {
    @NotNull private String mySource;

    public ICString(@NotNull final String source) {
      mySource = source;
    }

    public void updateSource(@NotNull final String newSource) {
      mySource = newSource;
    }

    @NotNull
    public String getSource() {
      return mySource;
    }

    public int compareTo(final ICString that) {
      return mySource.compareToIgnoreCase(that.mySource);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof ICString)) return false;
      return mySource.equalsIgnoreCase(((ICString)o).mySource);
    }

    @Override
    public int hashCode() {
      return mySource.toLowerCase().hashCode();
    }
  }
}
