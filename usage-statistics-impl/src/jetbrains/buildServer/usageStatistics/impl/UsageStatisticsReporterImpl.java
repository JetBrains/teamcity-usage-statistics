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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsReporterImpl implements UsageStatisticsReporter {
  @NotNull private static final Logger LOG = Logger.getLogger(UsageStatisticsReporterImpl.class);

  @NotNull private final UsageStatisticsCollector myStatisticsCollector;
  @NotNull private final UsageStatisticsCommonDataPersistor myCommonDataPersistor;

  public UsageStatisticsReporterImpl(@NotNull final UsageStatisticsCollector statisticsCollector,
                                     @NotNull final UsageStatisticsCommonDataPersistor commonDataPersistor) {
    myStatisticsCollector = statisticsCollector;
    myCommonDataPersistor = commonDataPersistor;
  }

  public boolean reportStatistics() {
    myStatisticsCollector.collectStatisticsAndWait();
    return doReportStatistics(createDataString(collectStatistics()));
  }

  private boolean doReportStatistics(@NotNull final String data) {
    final String serverUrl = TeamCityProperties.getProperty("teamcity.usageStatistics.server.url", "http://teamcity-stats.jetbrains.net/report.html");
    try {
      final URLConnection urlConnection = new URL(serverUrl).openConnection();
      if (!(urlConnection instanceof HttpURLConnection)) {
        LOG.debug("Invalid protocol: " + serverUrl);
        return false;
      }

      final HttpURLConnection connection = (HttpURLConnection) urlConnection;
      connection.setRequestMethod("POST");

      final byte[] bytes = data.getBytes("UTF-8");

      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
      final OutputStream outputStream = connection.getOutputStream();
      outputStream.write(bytes);
      outputStream.flush();          

      connection.connect();

      final int responseCode = connection.getResponseCode();

      LOG.debug("Usage statistics report sent. Server response: " + responseCode + " " + connection.getResponseMessage());
      logErrorAndClose(connection.getErrorStream());

      return responseCode == HttpURLConnection.HTTP_OK;
    }
    catch (final MalformedURLException e) {
      LOG.debug("Invalid usage statistics server URL: " + serverUrl, e);
    }
    catch (final IOException e) {
      LOG.debug("Failed to connect to usage statistics server: " + serverUrl, e);
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

    final Date lastReportingDate = myCommonDataPersistor.getLastReportingDate();
    if (lastReportingDate != null) {
      myStatistics.put("jb.previousReportDate", String.valueOf(lastReportingDate.getTime()));
    }

    return myStatistics;
  }
}
