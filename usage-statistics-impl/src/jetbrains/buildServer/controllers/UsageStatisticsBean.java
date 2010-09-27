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

package jetbrains.buildServer.controllers;

import java.util.*;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsCollectorImpl;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsBean {
  private final boolean myReportingEnabled;
  private final boolean myCollectingNow;
  private final boolean myStatisticsCollected;
  private final String mySizeEstimate;
  private final Date myLastCollectingFinishDate;
  private final Map<String, List<UsageStatisticPresentation>> myStatistics;

  public UsageStatisticsBean(@NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                             @NotNull final UsageStatisticsCollector statisticsCollector,
                             @NotNull final UsageStatisticsPresentationManagerEx presentationManager) {
    myReportingEnabled = settingsPersistor.loadSettings().isReportingEnabled();
    myCollectingNow = statisticsCollector.isCollectingNow();
    myStatisticsCollected = statisticsCollector.isStatisticsCollected();

    if (myStatisticsCollected) {
      myLastCollectingFinishDate = statisticsCollector.getLastCollectingFinishDate();
      myStatistics = new TreeMap<String, List<UsageStatisticPresentation>>();

      final int[] sizeEstimate = new int[] { 0 };
      statisticsCollector.publishCollectedStatistics(new UsageStatisticsPublisher() {
        public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
          final UsageStatisticPresentation presentation = presentationManager.createPresentation(id, value);
          getOrCreateGroup(presentation.getGroupName()).add(presentation);
          sizeEstimate[0] += id.length() + String.valueOf(value).length() + 3;
        }
      });

      mySizeEstimate = StringUtil.formatFileSize(sizeEstimate[0]);
    }
    else {
      myLastCollectingFinishDate = null;
      myStatistics = null;
      mySizeEstimate = null;
    }
  }

  public boolean isReportingEnabled() {
    return myReportingEnabled;
  }

  public boolean isCollectingNow() {
    return myCollectingNow;
  }

  public boolean isStatisticsCollected() {
    return myStatisticsCollected;
  }

  @NotNull
  public Date getLastCollectingFinishDate() {
    if (myLastCollectingFinishDate == null) {
      throw UsageStatisticsCollectorImpl.createIllegalStateException();
    }
    return myLastCollectingFinishDate;
  }

  @NotNull
  public Map<String, List<UsageStatisticPresentation>> getStatistics() {
    if (myStatistics == null) {
      throw UsageStatisticsCollectorImpl.createIllegalStateException();
    }
    return myStatistics;
  }

  @NotNull
  public String getSizeEstimate() {
    if (mySizeEstimate == null) {
      throw UsageStatisticsCollectorImpl.createIllegalStateException();
    }
    return mySizeEstimate;
  }

  @NotNull
  private List<UsageStatisticPresentation> getOrCreateGroup(@NotNull final String groupName) {
    if (!myStatistics.containsKey(groupName)) {
      myStatistics.put(groupName, new ArrayList<UsageStatisticPresentation>());
    }
    return myStatistics.get(groupName);
  }
}
