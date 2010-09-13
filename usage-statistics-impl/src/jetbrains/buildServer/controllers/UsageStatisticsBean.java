package jetbrains.buildServer.controllers;

import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 24.08.2010
 */
public class UsageStatisticsBean {
  private final boolean myReportingEnabled;
  @NotNull private final List<Statistic> myStatistics;

  public UsageStatisticsBean(@NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                             @NotNull final UsageStatisticsCollector statisticsCollector) {
    myReportingEnabled = settingsPersistor.loadSettings().isReportingEnabled();
    myStatistics = new ArrayList<Statistic>();
    statisticsCollector.collectStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @NotNull final String displayName, @Nullable final Object value) {
        myStatistics.add(new Statistic(displayName, value));
      }
    });
  }

  public boolean isReportingEnabled() {
    return myReportingEnabled;
  }

  @NotNull
  public List<Statistic> getStatistics() {
    return myStatistics;
  }

  public static class Statistic {
    @NotNull private final String myDisplayName;
    @Nullable private final Object myValue;

    public Statistic(@NotNull final String displayName, @Nullable final Object value) {
      myDisplayName = displayName;
      myValue = value;
    }

    @NotNull
    public String getDisplayName() {
      return myDisplayName;
    }

    @Nullable
    public Object getValue() {
      return myValue;
    }
  }
}
