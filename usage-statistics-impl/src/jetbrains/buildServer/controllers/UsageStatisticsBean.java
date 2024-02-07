
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