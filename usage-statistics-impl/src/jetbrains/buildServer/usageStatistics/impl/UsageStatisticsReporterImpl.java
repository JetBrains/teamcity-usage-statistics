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

package jetbrains.buildServer.usageStatistics.impl;

import com.intellij.openapi.diagnostic.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.HTTPRequestBuilder;
import jetbrains.buildServer.util.XmlUtil;
import jetbrains.buildServer.web.util.WebUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsReporterImpl implements UsageStatisticsReporter {
  @NotNull private static final Logger LOG = Logger.getInstance(UsageStatisticsReporterImpl.class.getName());

  @NotNull private final UsageStatisticsCollector myStatisticsCollector;
  @NotNull private final UsageStatisticsCommonDataPersistor myCommonDataPersistor;
  @NotNull private final HTTPRequestBuilder.RequestHandler myRequestHandler;

  public UsageStatisticsReporterImpl(@NotNull final UsageStatisticsCollector statisticsCollector,
                                     @NotNull final UsageStatisticsCommonDataPersistor commonDataPersistor,
                                     @NotNull final HTTPRequestBuilder.RequestHandler requestHandler) {
    myStatisticsCollector = statisticsCollector;
    myCommonDataPersistor = commonDataPersistor;
    myRequestHandler = requestHandler;
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
      final AtomicReference<String> result = new AtomicReference<>();
      final HTTPRequestBuilder.Request post =
        new HTTPRequestBuilder(TeamCityProperties.getProperty("teamcity.usageStatistics.server.url", "https://teamcity-stats.services.jetbrains.com/report.html"))
          .allowNonSecureConnection(true)
          .withDomainCheck(TeamCityProperties.getBooleanOrTrue("teamcity.usageStatistics.server.checkDomain"))
          .withMethod("POST")
          .withUrlEncodedData(data.getBytes("UTF-8"))
          .onErrorResponse((state, text) -> {
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
          .onSuccess(response -> result.set(response.getBodyAsString()))
          .onException(ex -> {
            LOG.warnAndDebugDetails("Cannot send usage statistics", ex);
          })
          .build();

      myRequestHandler.doRequest(post);

      if (result.get() == null) return false;

      if (result.get().isEmpty()) return true; // legacy

      final Element element = XmlUtil.from_s(result.get());
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
