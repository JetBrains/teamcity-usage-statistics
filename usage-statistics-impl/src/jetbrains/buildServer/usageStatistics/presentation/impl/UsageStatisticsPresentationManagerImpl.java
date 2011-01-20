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

package jetbrains.buildServer.usageStatistics.presentation.impl;

import java.util.*;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.*;
import jetbrains.buildServer.usageStatistics.presentation.renderers.DefaultUsageStatisticsGroup;
import jetbrains.buildServer.util.MultiMap;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsPresentationManagerImpl implements UsageStatisticsPresentationManagerEx {
  @NotNull private static final String MISCELLANEOUS = "Miscellaneous";
  @NotNull private static final Comparator<String> STRINGS_COMPARATOR = new Comparator<String>() {
    public int compare(@NotNull final String s1, @NotNull final String s2) {
      return s1.compareToIgnoreCase(s2);
    }
  };

  @NotNull private final PluginDescriptor myPluginDescriptor;

  @NotNull private final Map<String, String> myStatisticGroups = new HashMap<String, String>(); // statistic id -> group name
  @NotNull private final Map<String, UsageStatisticsGroupExtension> myGroupExtensions = new HashMap<String, UsageStatisticsGroupExtension>(); // group name -> extension
  @NotNull private final Map<String, UsageStatisticsPresentationFactory> myPresentationFactories = new HashMap<String, UsageStatisticsPresentationFactory>(); // statistic id -> factory

  public UsageStatisticsPresentationManagerImpl(@NotNull final PluginDescriptor pluginDescriptor) {
    myPluginDescriptor = pluginDescriptor;
  }

  public void applyPresentation(@NotNull final String id,
                                @Nullable final String displayName,
                                @Nullable final String groupName,
                                @Nullable final UsageStatisticsFormatter formatter) {
    doApplyPresentation(id, displayName, formatter);
    if (groupName != null) {
      myStatisticGroups.put(id, groupName);
    }
  }

  public void registerGroupRenderer(@NotNull final String groupName, @NotNull final UsageStatisticsGroupExtension extension) {
    myGroupExtensions.put(groupName, extension);
  }

  @NotNull
  public LinkedHashMap<String, UsageStatisticsGroupExtension> groupStatistics(@NotNull final UsageStatisticsCollector collector) {
    final MultiMap<String, UsageStatisticPresentation> groupedStatistics = new MultiMap<String, UsageStatisticPresentation>(); // group name -> collection of statistics
    collector.publishCollectedStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        groupedStatistics.putValue(getGroupName(id), getPresentationFactory(id).createFor(value));
      }
    });

    final List<String> groupNames = new ArrayList<String>(groupedStatistics.keySet());
    Collections.sort(groupNames, STRINGS_COMPARATOR);

    final LinkedHashMap<String, UsageStatisticsGroupExtension> groups = new LinkedHashMap<String, UsageStatisticsGroupExtension>();

    for (final String groupName : groupNames) {
      final UsageStatisticsGroupExtension group = getGroupExtension(groupName);
      group.setStatistics(groupedStatistics.get(groupName));
      groups.put(groupName, group);
    }

    return groups;
  }

  @NotNull
  private String getGroupName(@NotNull final String id) {
    if (!myStatisticGroups.containsKey(id)) {
      myStatisticGroups.put(id, MISCELLANEOUS);
    }
    return myStatisticGroups.get(id);
  }

  @NotNull
  private UsageStatisticsGroupExtension getGroupExtension(@NotNull final String groupName) {
    if (!myGroupExtensions.containsKey(groupName)) {
      registerGroupRenderer(groupName, new DefaultUsageStatisticsGroup(myPluginDescriptor));
    }
    return myGroupExtensions.get(groupName);
  }

  @NotNull
  private UsageStatisticsPresentationFactory getPresentationFactory(@NotNull final String id) {
    if (!myPresentationFactories.containsKey(id)) {
      doApplyPresentation(id, null, null);
    }
    return myPresentationFactories.get(id);
  }

  private void doApplyPresentation(@NotNull final String id, @Nullable final String displayName, @Nullable final UsageStatisticsFormatter formatter) {
    myPresentationFactories.put(id, new UsageStatisticsPresentationFactory(id, displayName, formatter));
  }
}
