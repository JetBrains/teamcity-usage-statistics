package jetbrains.buildServer.controllers;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsCollectorImpl;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Maxim.Manuylov
 *         Date: 24.09.2010
 */
public class DownloadUsageStatisticsController extends BaseController {
  @NotNull private static final Logger LOG = Logger.getLogger(DownloadUsageStatisticsController.class);
  @NotNull private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");

  @NotNull private final UsageStatisticsCollector myStatisticsCollector;

  public DownloadUsageStatisticsController(@NotNull final SBuildServer server,
                                           @NotNull final AuthorizationInterceptor authInterceptor,
                                           @NotNull final WebControllerManager webControllerManager,
                                           @NotNull final UsageStatisticsCollector statisticsCollector) {
    super(server);
    myStatisticsCollector = statisticsCollector;

    UsageStatisticsControllerUtil.register(this, authInterceptor, webControllerManager, "/admin/downloadUsageStatistics.html");
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    if (!myStatisticsCollector.isStatisticsCollected()) {
      //noinspection ThrowableResultOfMethodCallIgnored
      final String cause = UsageStatisticsCollectorImpl.createIllegalStateException().getLocalizedMessage().toLowerCase();
      WebUtil.notFound(response, "Failed to download usage statistics: " + cause, LOG);
      return null;
    }

    final String baseFileName = String.format("tc-usage-statistics-%s.", DATE_FORMAT.format(myStatisticsCollector.getLastCollectingFinishDate()));

    final boolean archived = "true".equals(request.getParameter("archived"));

    PrintWriter out = null;
    try {
      final String contentType, extension;

      if (archived) {
        final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
        zipOutputStream.putNextEntry(new ZipEntry(baseFileName + "txt"));

        out = new PrintWriter(zipOutputStream);
        contentType = "application/zip";
        extension = "zip";
      }
      else {
        out = response.getWriter();
        contentType = "text/plain";
        extension = "txt";
      }

      response.setContentType(contentType);
      WebUtil.setContentDisposition(response, baseFileName + extension, false);
      WebUtil.addCacheHeadersForIE(request, response);

      writeStatistics(out);
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

  private void writeStatistics(@NotNull final PrintWriter out) {
    myStatisticsCollector.publishCollectedStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        out.write(id);
        out.write("=");
        out.write(String.valueOf(value));
        out.write("\r\n");
      }
    });
  }
}
