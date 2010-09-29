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

package jetbrains.buildServer.usageStatistics.impl;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.util.Dates;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsReportingScheduler extends BuildServerAdapter implements Runnable {
  @NotNull private static final Logger LOG = Logger.getLogger(UsageStatisticsReportingScheduler.class);

  private static final String CHECKING_INTERVAL = "teamcity.usageStatistics.checking.interval.minutes";
  private static final int DEFAULT_CHECKING_INTERVAL = 60; // hour

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
    final long checkingInterval = TeamCityProperties.getInteger(CHECKING_INTERVAL, DEFAULT_CHECKING_INTERVAL) * Dates.ONE_MINUTE;
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
        if (lastReportingDate == null || Dates.now().after(Dates.after(lastReportingDate, getReportingPeriod()))) {
          if (myStatisticsReporter.reportStatistics()) {
            myCommonDataPersistor.setLastReportingDate(Dates.now());
          }
        }
      }
    }
    catch (final Throwable e) {
      LOG.debug("Cannot report usage statistics: ", e);
    }
  }

  private long getReportingPeriod() {
    return TeamCityProperties.getInteger(REPORTING_PERIOD, DEFAULT_REPORTING_PERIOD) * Dates.ONE_MINUTE;
  }
}