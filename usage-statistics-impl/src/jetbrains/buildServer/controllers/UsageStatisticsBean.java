/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import com.intellij.openapi.util.Pair;
import java.util.Date;
import java.util.LinkedHashMap;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsCollectorImpl;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroup;
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
  private final LinkedHashMap<String, Pair<String, UsageStatisticsGroup>> myStatisticGroups;

  public UsageStatisticsBean(@NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                             @NotNull final UsageStatisticsCollector statisticsCollector,
                             @NotNull final UsageStatisticsPresentationManagerEx presentationManager) {
    myReportingEnabled = settingsPersistor.loadSettings().isReportingEnabled();
    myCollectingNow = statisticsCollector.isCollectingNow();
    myStatisticsCollected = statisticsCollector.isStatisticsCollected();

    if (myStatisticsCollected) {
      myLastCollectingFinishDate = statisticsCollector.getLastCollectingFinishDate();
      myStatisticGroups = presentationManager.groupStatistics(statisticsCollector);

      final int[] sizeEstimate = new int[] { 0 };
      statisticsCollector.publishCollectedStatistics(new UsageStatisticsPublisher() {
        public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
          sizeEstimate[0] += id.length() + String.valueOf(value).length() + 3;
        }
      });

      mySizeEstimate = StringUtil.formatFileSize(sizeEstimate[0]);
    }
    else {
      myLastCollectingFinishDate = null;
      myStatisticGroups = null;
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
  public LinkedHashMap<String, Pair<String, UsageStatisticsGroup>> getStatisticGroups() {
    if (myStatisticGroups == null) {
      throw UsageStatisticsCollectorImpl.createIllegalStateException();
    }
    return myStatisticGroups;
  }

  @NotNull
  public String getSizeEstimate() {
    if (mySizeEstimate == null) {
      throw UsageStatisticsCollectorImpl.createIllegalStateException();
    }
    return mySizeEstimate;
  }
}
