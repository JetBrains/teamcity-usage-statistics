
package jetbrains.buildServer.usageStatistics.impl;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsReportingScheduler extends BuildServerAdapter implements Runnable {
  @NotNull private static final Logger LOG = Logger.getInstance(UsageStatisticsReportingScheduler.class.getName());

  @NotNull private static final String REPORTING_PERIOD = "teamcity.usageStatistics.reporting.period.minutes";
  private static final int DEFAULT_REPORTING_PERIOD = 24 * 60; // day

  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCommonDataPersistor myCommonDataPersistor;
  @NotNull private final UsageStatisticsReporter myStatisticsReporter;
  @NotNull private final ScheduledFuture<?> myTask;

  public UsageStatisticsReportingScheduler(@NotNull final SBuildServer server,
                                           @NotNull final ScheduledExecutorService executor,
                                           @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                           @NotNull final UsageStatisticsCommonDataPersistor commonDataPersistor,
                                           @NotNull final UsageStatisticsReporter statisticsReporter) {
    mySettingsPersistor = settingsPersistor;
    myCommonDataPersistor = commonDataPersistor;
    myStatisticsReporter = statisticsReporter;
    final long checkingInterval = Math.max(Dates.ONE_MINUTE, getReportingPeriod() / 100);
    myTask = executor.scheduleAtFixedRate(this, checkingInterval, checkingInterval, TimeUnit.MILLISECONDS);
    server.addListener(this);
  }

  @Override
  public void serverShutdown() {
    myTask.cancel(true);
  }

  public void run() {
    try {
      if (mySettingsPersistor.loadSettings().isReportingEnabled()) {
        final Date lastReportingDate = myCommonDataPersistor.getLastReportingDate();
        final long reportingPeriod = getReportingPeriod();
        if (lastReportingDate == null || Dates.now().after(Dates.after(lastReportingDate, reportingPeriod))) {
          if (myStatisticsReporter.reportStatistics(reportingPeriod)) {
            myCommonDataPersistor.setLastReportingDate(Dates.now());
            LOG.debug("Usage statistics was successfully reported to JetBrains.");
          }
        }
      }
    }
    catch (final Throwable e) {
      LOG.infoAndDebugDetails("Error collecting/reporting usage statistics: ", e);
    }
  }

  private long getReportingPeriod() {
    return TeamCityProperties.getLong(REPORTING_PERIOD, DEFAULT_REPORTING_PERIOD) * Dates.ONE_MINUTE;
  }
}