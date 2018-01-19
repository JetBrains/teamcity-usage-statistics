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

import com.intellij.openapi.util.UserDataHolder;
import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseUsageStatisticsProvider implements UsageStatisticsProvider, UsageStatisticsPresentationProvider {
  @NotNull private String myIdFormat;
  @NotNull protected String myGroupName;
  @NotNull private final PresentationsCollector myPresentationsCollector = new PresentationsCollector();

  public void setIdFormat(@NotNull final String idFormat) {
    myIdFormat = idFormat;
  }

  public void setGroupName(@NotNull final String groupName) {
    myGroupName = groupName;
  }

  @NotNull
  protected String makeId(@NotNull final String... params) {
    return String.format(myIdFormat, params);
  }

  protected abstract void accept(@NotNull UsageStatisticsPublisher publisher,
                                 @NotNull UsageStatisticsPresentationManager presentationManager);

  @NotNull
  protected abstract PositionAware getGroupPosition();

  protected abstract void setupGroup(@NotNull final UsageStatisticsPresentationManager presentationManager);

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    myPresentationsCollector.clear();
    accept(publisher, myPresentationsCollector);
  }

  public void accept(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    setupGroup(presentationManager);
    myPresentationsCollector.applyTo(presentationManager);
  }

  private static class PresentationsCollector implements UsageStatisticsPresentationManager {
    @NotNull private final List<StatisticPresentation> myStatisticPresentations = new ArrayList<StatisticPresentation>();
    @NotNull private final List<GroupPresentation> myGroupPresentations = new ArrayList<GroupPresentation>();

    public void applyPresentation(@NotNull final String id,
                                  @Nullable final String displayName,
                                  @Nullable final String groupName,
                                  @Nullable final UsageStatisticsFormatter formatter,
                                  @Nullable final String valueTooltip) {
      myStatisticPresentations.add(new StatisticPresentation(id, displayName, groupName, formatter, valueTooltip));
    }

    public void setGroupType(@NotNull final String groupName,
                             @NotNull final String groupTypeId,
                             @NotNull final PositionAware groupPosition,
                             @Nullable final UserDataHolder groupSettings) {
      myGroupPresentations.add(new GroupPresentation(groupName, groupTypeId, groupPosition, groupSettings));
    }

    void clear() {
      myStatisticPresentations.clear();
      myGroupPresentations.clear();
    }

    void applyTo(@NotNull final UsageStatisticsPresentationManager presentationManager) {
      for (final StatisticPresentation sp : myStatisticPresentations) {
        presentationManager.applyPresentation(sp.getId(), sp.getDisplayName(), sp.getGroupName(), sp.getFormatter(), sp.getValueTooltip());
      }
      for (final GroupPresentation gp : myGroupPresentations) {
        presentationManager.setGroupType(gp.getGroupName(), gp.getGroupTypeId(), gp.getGroupPosition(), gp.getGroupSettings());
      }
    }
  }

  private static class StatisticPresentation {
    @NotNull private final String myId;
    @Nullable private final String myDisplayName;
    @Nullable private final String myGroupName;
    @Nullable private final UsageStatisticsFormatter myFormatter;
    @Nullable private final String myValueTooltip;

    StatisticPresentation(@NotNull final String id,
                          @Nullable final String displayName,
                          @Nullable final String groupName,
                          @Nullable final UsageStatisticsFormatter formatter,
                          @Nullable final String valueTooltip) {
      myId = id;
      myDisplayName = displayName;
      myGroupName = groupName;
      myFormatter = formatter;
      myValueTooltip = valueTooltip;
    }

    @NotNull
    String getId() {
      return myId;
    }

    @Nullable
    String getDisplayName() {
      return myDisplayName;
    }

    @Nullable
    String getGroupName() {
      return myGroupName;
    }

    @Nullable
    UsageStatisticsFormatter getFormatter() {
      return myFormatter;
    }

    @Nullable
    String getValueTooltip() {
      return myValueTooltip;
    }
  }

  private static class GroupPresentation {
    @NotNull private final String myGroupName;
    @NotNull private final String myGroupTypeId;
    @NotNull private final PositionAware myGroupPosition;
    @Nullable private final UserDataHolder myGroupSettings;

    GroupPresentation(@NotNull final String groupName,
                      @NotNull final String groupTypeId,
                      @NotNull final PositionAware groupPosition,
                      @Nullable final UserDataHolder groupSettings) {
      myGroupName = groupName;
      myGroupTypeId = groupTypeId;
      myGroupPosition = groupPosition;
      myGroupSettings = groupSettings;
    }

    @NotNull
    String getGroupName() {
      return myGroupName;
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
