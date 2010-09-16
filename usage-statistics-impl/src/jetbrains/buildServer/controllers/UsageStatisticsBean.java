package jetbrains.buildServer.controllers;

import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.usageStatistics.Formatter;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.usageStatistics.impl.formatters.FormatUtil;
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
      public void publishStatistic(@NotNull final String id,
                                   @NotNull final String displayName,
                                   @Nullable final Object value,
                                   @Nullable final Formatter formatter,
                                   @Nullable final String groupName) {
        myStatistics.add(new Statistic(displayName, value, formatter));
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
    @NotNull private final String myFormattedValue;

    public Statistic(@NotNull final String displayName, @Nullable final Object value, @Nullable final Formatter formatter) {
      myDisplayName = displayName;
      myFormattedValue = FormatUtil.format(formatter, value);
    }

    @NotNull
    public String getDisplayName() {
      return myDisplayName;
    }

    @Nullable
    public String getFormattedValue() {
      return myFormattedValue;
    }
  }
}
