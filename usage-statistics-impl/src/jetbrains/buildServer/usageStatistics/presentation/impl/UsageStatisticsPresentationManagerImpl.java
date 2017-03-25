/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.*;
import jetbrains.buildServer.util.MultiMap;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionAwareCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsPresentationManagerImpl implements UsageStatisticsPresentationManagerEx {
  @NotNull private static final String MISCELLANEOUS = "Miscellaneous";

  @NotNull private final Map<String, String> myStatisticGroups = new HashMap<String, String>(); // statistic id -> group name
  @NotNull private final Map<String, GroupInfo> myGroupInfos = new HashMap<String, GroupInfo>(); // group name -> group info
  @NotNull private final Map<String, UsageStatisticsPresentationFactory> myPresentationFactories = new HashMap<String, UsageStatisticsPresentationFactory>(); // statistic id -> factory

  @NotNull private final ExtensionHolder myExtensionHolder;

  public UsageStatisticsPresentationManagerImpl(@NotNull final ExtensionHolder extensionHolder) {
    myExtensionHolder = extensionHolder;
  }

  public void applyPresentation(@NotNull final String id,
                                @Nullable final String displayName,
                                @Nullable final String groupName,
                                @Nullable final UsageStatisticsFormatter formatter,
                                @Nullable final String valueTooltip) {
    doApplyPresentation(id, displayName, formatter, valueTooltip);
    if (groupName != null) {
      myStatisticGroups.put(id, groupName);
    }
  }

  public void setGroupType(@NotNull final String groupName,
                           @NotNull final String groupTypeId,
                           @NotNull final PositionAware groupPosition,
                           @Nullable final UserDataHolder groupSettings) {
    myGroupInfos.put(groupName, new GroupInfo(groupTypeId, groupPosition, groupSettings));
  }

  @NotNull
  public LinkedHashMap<String, Pair<String, UsageStatisticsGroup>> groupStatistics(@NotNull final UsageStatisticsCollector collector) {
    final MultiMap<String, UsageStatisticPresentation> groupedStatistics = new MultiMap<String, UsageStatisticPresentation>(); // group name -> collection of statistics
    collector.publishCollectedStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        groupedStatistics.putValue(getGroupName(id), getPresentationFactory(id).createFor(value));
      }
    });

    final PositionAwareCollection<Pair<String, GroupInfo>> groupInfos = new PositionAwareCollection<Pair<String, GroupInfo>>();
    for (final String groupName : groupedStatistics.keySet()) {
      final GroupInfo groupInfo = getGroupInfo(groupName);
      groupInfos.add(Pair.create(groupName, groupInfo), groupInfo.getGroupPosition());
    }

    final Map<String, UsageStatisticsGroupType> groupTypes = collectGroupTypes();

    final LinkedHashMap<String, Pair<String, UsageStatisticsGroup>> groups = new LinkedHashMap<String, Pair<String, UsageStatisticsGroup>>();
    for (final Pair<String, GroupInfo> pair : groupInfos.getAllSorted()) {
      final String groupName = pair.getFirst();
      final GroupInfo groupInfo = pair.getSecond();
      final UsageStatisticsGroupType groupType = groupTypes.get(groupInfo.getGroupTypeId());
      if (groupType == null) continue;
      final UsageStatisticsGroup group = groupType.createGroup(groupInfo.getGroupSettings());
      group.setStatistics(groupedStatistics.get(groupName));
      groups.put(groupName, Pair.create(groupType.getJspPagePath(), group));
    }

    return groups;
  }

  @NotNull
  private Map<String, UsageStatisticsGroupType> collectGroupTypes() {
    final Map<String, UsageStatisticsGroupType> result = new HashMap<String, UsageStatisticsGroupType>();
    for (final UsageStatisticsGroupType groupType : myExtensionHolder.getExtensions(UsageStatisticsGroupType.class)) {
      result.put(groupType.getId(), groupType);
    }
    return result;
  }

  @NotNull
  private String getGroupName(@NotNull final String id) {
    if (!myStatisticGroups.containsKey(id)) {
      myStatisticGroups.put(id, MISCELLANEOUS);
    }
    return myStatisticGroups.get(id);
  }

  @NotNull
  private UsageStatisticsPresentationFactory getPresentationFactory(@NotNull final String id) {
    if (!myPresentationFactories.containsKey(id)) {
      doApplyPresentation(id, null, null, null);
    }
    return myPresentationFactories.get(id);
  }

  @NotNull
  private GroupInfo getGroupInfo(@NotNull final String groupName) {
    if (!myGroupInfos.containsKey(groupName)) {
      setGroupType(groupName, UsageStatisticsGroupType.DEFAULT, UsageStatisticsGroupPosition.DEFAULT, null);
    }
    return myGroupInfos.get(groupName);
  }

  private void doApplyPresentation(@NotNull final String id,
                                   @Nullable final String displayName,
                                   @Nullable final UsageStatisticsFormatter formatter,
                                   @Nullable final String valueTooltip) {
    myPresentationFactories.put(id, new UsageStatisticsPresentationFactory(id, displayName, formatter, valueTooltip));
  }

  private static class GroupInfo {
    @NotNull private final String myGroupTypeId;
    @NotNull private final PositionAware myGroupPosition;
    @Nullable private final UserDataHolder myGroupSettings;

    GroupInfo(@NotNull final String groupTypeId, @NotNull final PositionAware groupPosition, @Nullable final UserDataHolder groupSettings) {
      myGroupTypeId = groupTypeId;
      myGroupPosition = groupPosition;
      myGroupSettings = groupSettings;
    }

    @NotNull
    String getGroupTypeId() {
      return myGroupTypeId;
    }

    @NotNull
    PositionAware getGroupPosition() {
      return myGroupPosition;
    }

    @Nullable
    UserDataHolder getGroupSettings() {
      return myGroupSettings;
    }
  }
}
