/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsCollectorImpl;
import jetbrains.buildServer.util.SortedProperties;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

public class DownloadUsageStatisticsController extends BaseController {
  @NotNull private static final Logger LOG = Logger.getInstance(DownloadUsageStatisticsController.class.getName());
  @NotNull private static final SimpleDateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
  @NotNull private static final SimpleDateFormat FILE_CONTENT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  @NotNull private final WebLinks myWebLinks;
  @NotNull private final UsageStatisticsCollector myStatisticsCollector;

  public DownloadUsageStatisticsController(@NotNull final SBuildServer server,
                                           @NotNull final WebLinks webLinks,
                                           @NotNull final AuthorizationInterceptor authInterceptor,
                                           @NotNull final WebControllerManager webControllerManager,
                                           @NotNull final UsageStatisticsCollector statisticsCollector) {
    super(server);
    myWebLinks = webLinks;
    myStatisticsCollector = statisticsCollector;

    UsageStatisticsControllerUtil.register(this, authInterceptor, webControllerManager, "/admin/downloadUsageStatistics.html");
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    if (!myStatisticsCollector.isStatisticsCollected()) {
      //noinspection ThrowableResultOfMethodCallIgnored
      final String cause = UsageStatisticsCollectorImpl.createIllegalStateException().getLocalizedMessage().toLowerCase();
      WebUtil.notFound(request, response, "Failed to download usage statistics: " + cause, LOG);
      return null;
    }

    final Date collectingFinishDate = myStatisticsCollector.getLastCollectingFinishDate();
    final String fileName = String.format("tc-usage-statistics-%s.properties", FILE_NAME_DATE_FORMAT.format(collectingFinishDate));

    OutputStream out = null;
    try {
      out = response.getOutputStream();

      response.setContentType("text/plain");
      WebUtil.setContentDisposition(request, response, fileName, false);
      WebUtil.addCacheHeadersForIE(request, response);

      writeStatistics(out, collectingFinishDate);
    }
    catch (final Exception e) {
      response.sendError(500, "Failed to download usage statistics");
      LOG.error("Failed to download usage statistics", e);
    }
    finally {
      if (out != null) {
        out.flush();
        out.close();
      }
    }

    return null;
  }

  private void writeStatistics(@NotNull final OutputStream out, @NotNull final Date collectingFinishDate) throws IOException {
    final BufferedWriter writer = new BufferedWriter(new PrintWriter(out));

    writer.write("#TeamCity URL: " + myWebLinks.getRootUrl());
    writer.newLine();

    writer.write("#Usage statistics collecting finish date: " + FILE_CONTENT_DATE_FORMAT.format(collectingFinishDate));
    writer.newLine();

    writer.flush();

    final Properties properties = new SortedProperties();

    myStatisticsCollector.publishCollectedStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        properties.setProperty(id, String.valueOf(value));
      }
    });

    properties.store(out, null);
  }
}
