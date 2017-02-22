/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import java.io.*;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.HTTPRequestHelper;
import jetbrains.buildServer.util.XmlUtil;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsReporterImpl implements UsageStatisticsReporter {
  @NotNull private static final Logger LOG = Logger.getLogger(UsageStatisticsReporterImpl.class);

  @NotNull private final UsageStatisticsCollector myStatisticsCollector;
  @NotNull private final UsageStatisticsCommonDataPersistor myCommonDataPersistor;
  @NotNull private final HTTPRequestHelper myHTTPRequestHelper = new HTTPRequestHelper(
    TeamCityProperties.getInteger("teamcity.usageStatistics.timeout", (int)TimeUnit.MINUTES.toSeconds(5)));

  public UsageStatisticsReporterImpl(@NotNull final UsageStatisticsCollector statisticsCollector,
                                     @NotNull final UsageStatisticsCommonDataPersistor commonDataPersistor) {
    myStatisticsCollector = statisticsCollector;
    myCommonDataPersistor = commonDataPersistor;
  }

  public boolean reportStatistics(final long statisticsExpirationPeriod) {
    if (!myStatisticsCollector.isStatisticsCollected() || collectedStatisticsExpired(statisticsExpirationPeriod)) {
      myStatisticsCollector.collectStatisticsAndWait();
    }
    return doReportStatistics(createDataString(collectStatistics()));
  }

  private boolean collectedStatisticsExpired(final long statisticsExpirationPeriod) {
    return Dates.now().getTime() > myStatisticsCollector.getLastCollectingFinishDate().getTime() + statisticsExpirationPeriod;
  }

  private boolean doReportStatistics(@NotNull final String data) {
    try {
      final String result =
        myHTTPRequestHelper.request(TeamCityProperties.getProperty("teamcity.usageStatistics.server.url", "https://teamcity-stats.services.jetbrains.com/report.html"))
                           .allowNonSecureConnection(true)
                           .withDomainCheck(TeamCityProperties.getBooleanOrTrue("teamcity.usageStatistics.server.checkDomain"))
                           .withMethod("POST")
                           .withUrlEncodedData(data.getBytes("UTF-8"))
                           .onError((state, text) -> {
                             if (state == 404) {
                               LOG.info("Cannot send usage statistics: server unavailable");
                             } else {
                               if (text != null) {
                                 LOG.info("Cannot send usage statistics: " + text);
                               } else {
                                 LOG.info("Cannot send usage statistics: return code " + state);
                               }
                             }
                           })
                           .onException(ex -> {
                             if (LOG.isDebugEnabled()) {
                               LOG.debug("Cannot send usage statistics", ex);
                             } else {
                               LOG.warn("Cannot send usage statistics: " + ex.getMessage());
                             }
                           })
                           .doRequest();

      if (result == null) return false;

      if (result.isEmpty()) return true; // legacy

      final Element element = XmlUtil.from_s(result);
      if (element.getChild("ok") != null) {
        return true;
      }
      final Element ignored = element.getChild("ignored");
      if (ignored != null) {
        LOG.info("Usage statistics server has filtered the request: " + ignored.getText());
        return true;
      }
      final Element error = element.getChild("error");
      if (error != null) {
        LOG.info("Statistics server failed to process usage data: " + error.getText());
        return false;
      }

      return true;
    } catch (UnsupportedEncodingException ignore) {
    } catch (URISyntaxException e) {
      LOG.info("Cannot send usage statistics: " + e.getMessage());
    }

    return false;
  }

  private void logErrorAndClose(@Nullable final InputStream errorStream) {
    if (errorStream == null) return;
    final BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
    try {
      final StringBuilder sb = new StringBuilder("Usage statistics server error response:\n");
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      LOG.debug(sb.toString());
    }
    catch (final IOException e) {
      LOG.debug("Failed to read error stream", e);
    }
    finally {
      try {
        reader.close();
      } catch (final IOException e) {
        LOG.debug("Failed to close error stream", e);
      }
    }
  }

  @NotNull
  private static String createDataString(@NotNull final Map<String, String> statistics) {
    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<String, String> entry : statistics.entrySet()) {
      sb.append('&').append(WebUtil.encode(entry.getKey())).append('=').append(WebUtil.encode(entry.getValue()));
    }
    return sb.length() == 0 ? "" : sb.substring(1);
  }

  @NotNull
  private Map<String, String> collectStatistics() {
    final Map<String, String> myStatistics = new HashMap<String, String>();

    myStatisticsCollector.publishCollectedStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        myStatistics.put(id, String.valueOf(value));
      }
    });

    myStatistics.put("jb.collectingFinishDate", String.valueOf(myStatisticsCollector.getLastCollectingFinishDate().getTime()));

    final Date lastReportingDate = myCommonDataPersistor.getLastReportingDate();
    if (lastReportingDate != null) {
      myStatistics.put("jb.previousReportDate", String.valueOf(lastReportingDate.getTime()));
    }

    return myStatistics;
  }
}
