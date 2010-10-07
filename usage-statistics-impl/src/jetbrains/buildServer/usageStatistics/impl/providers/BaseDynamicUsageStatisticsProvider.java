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
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import jetbrains.buildServer.usageStatistics.presentation.renderers.DynamicUsageStatisticsGroup;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

abstract class BaseDynamicUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private final LinkedHashMap<Long, String> myPeriodDescriptions;
  @NotNull private final Map<String, String> myDefaultValues; // group name -> default value

  protected BaseDynamicUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                               @NotNull final UsageStatisticsPresentationManager presentationManager,
                                               @NotNull final PluginDescriptor pluginDescriptor,
                                               @NotNull final LinkedHashMap<Long, String> periodDescriptions,
                                               @NotNull final Set<String> dynamicGroupNames) {
    super(server, presentationManager);
    myPeriodDescriptions = periodDescriptions;
    myDefaultValues = new HashMap<String, String>();
    applyPresentations(presentationManager);
    registerGroupRenderers(presentationManager, pluginDescriptor, periodDescriptions, dynamicGroupNames);
  }

  protected BaseDynamicUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                               @NotNull final UsageStatisticsPresentationManager presentationManager,
                                               @NotNull final PluginDescriptor pluginDescriptor,
                                               @NotNull final Set<String> dynamicGroupNames) {
    this(server, presentationManager, pluginDescriptor, createDefaultPeriodDescriptions(), dynamicGroupNames);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final long now = Dates.now().getTime();
    for (final Long period : myPeriodDescriptions.keySet()) {
      accept(publisher, myPeriodDescriptions.get(period), now - period);
    }
  }

  @NotNull
  protected static LinkedHashMap<Long, String> createDefaultPeriodDescriptions() {
    return new LinkedHashMap<Long, String>() {{
      put(Dates.ONE_HOUR, "Hour");
      put(Dates.ONE_DAY, "Day");
      put(Dates.ONE_WEEK, "Week");
    }};
  }

  protected void setDefaultValue(@NotNull final String groupName, @NotNull final String defaultValue) {
    myDefaultValues.put(groupName, defaultValue);
  }

  protected long getThresholdDate() {
    long maxPeriod = 0;
    for (final Long period : myPeriodDescriptions.keySet()) {
      if (period > maxPeriod) {
        maxPeriod = period;
      }
    }
    return Dates.now().getTime() - maxPeriod;
  }

  protected abstract void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull String periodDescription, long startDate);

  protected abstract boolean mustSortStatistics();

  protected void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager, @NotNull final String periodDescription) {}

  private void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    for (final String periodDescription : myPeriodDescriptions.values()) {
      applyPresentations(presentationManager, periodDescription);
    }
  }

  private void registerGroupRenderers(@NotNull final UsageStatisticsPresentationManager presentationManager,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final LinkedHashMap<Long, String> periodDescriptions,
                                      @NotNull final Set<String> dynamicGroupNames) {
    final List<String> periods = new ArrayList<String>(periodDescriptions.size());

    for (final Map.Entry<Long, String> entry : periodDescriptions.entrySet()) {
      periods.add(entry.getValue());
    }

    final String defaultDefaultValue = new DefaultFormatter().format(null);

    for (final String groupName : dynamicGroupNames) {
      final DynamicUsageStatisticsGroup.DefaultValueProvider defaultValueProvider = createDefaultValueProvider(groupName, defaultDefaultValue);
      final DynamicUsageStatisticsGroup group = new DynamicUsageStatisticsGroup(pluginDescriptor, periods, defaultValueProvider, mustSortStatistics());
      presentationManager.registerGroupRenderer(groupName, group);
    }
  }

  private DynamicUsageStatisticsGroup.DefaultValueProvider createDefaultValueProvider(@NotNull final String groupName, @NotNull final String defaultDefaultValue) {
    return new DynamicUsageStatisticsGroup.DefaultValueProvider() {
      @NotNull
      public String getDefaultValue() {
        final String defaultValue = myDefaultValues.get(groupName);
        return defaultValue == null ? defaultDefaultValue : defaultValue;
      }
    };
  }
}