/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import jetbrains.buildServer.usageStatistics.presentation.renderers.DynamicUsageStatisticsGroup;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

abstract class BaseDynamicUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull protected final UsageStatisticsPresentationManager myPresentationManager;
  @NotNull private final PluginDescriptor myPluginDescriptor;
  @NotNull private final LinkedHashMap<Long, String> myPeriodDescriptions;
  @NotNull private final Map<String, String> myDefaultValues; // group name -> default value

  protected BaseDynamicUsageStatisticsProvider(@NotNull final UsageStatisticsPresentationManager presentationManager,
                                               @NotNull final PluginDescriptor pluginDescriptor,
                                               @NotNull final LinkedHashMap<Long, String> periodDescriptions) {
    myPresentationManager = presentationManager;
    myPeriodDescriptions = periodDescriptions;
    myPluginDescriptor = pluginDescriptor;
    myDefaultValues = new HashMap<String, String>();
  }

  @Override
  public void setGroupName(@NotNull final String groupName) {
    super.setGroupName(groupName);
    registerGroupRenderer(myPresentationManager, myPluginDescriptor, myPeriodDescriptions, groupName);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final long now = Dates.now().getTime();
    for (final Long period : myPeriodDescriptions.keySet()) {
      accept(publisher, myPeriodDescriptions.get(period).toLowerCase(), now - period);
    }
  }

  @NotNull
  protected static LinkedHashMap<Long, String> createDWMPeriodDescriptions() {
    return new LinkedHashMap<Long, String>() {{
      put(Dates.ONE_DAY, "Day");
      put(Dates.ONE_WEEK, "Week");
      put(30 * Dates.ONE_DAY, "Month");
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

  private void registerGroupRenderer(@NotNull final UsageStatisticsPresentationManager presentationManager,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final LinkedHashMap<Long, String> periodDescriptions,
                                     @NotNull final String groupName) {
    final List<String> periods = new ArrayList<String>(periodDescriptions.size());

    for (final Map.Entry<Long, String> entry : periodDescriptions.entrySet()) {
      periods.add(entry.getValue());
    }

    final DynamicUsageStatisticsGroup.DefaultValueProvider defaultValueProvider = createDefaultValueProvider(groupName, new DefaultFormatter().format(null));
    final DynamicUsageStatisticsGroup group = new DynamicUsageStatisticsGroup(pluginDescriptor, periods, defaultValueProvider, mustSortStatistics());

    presentationManager.registerGroupRenderer(groupName, group);
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