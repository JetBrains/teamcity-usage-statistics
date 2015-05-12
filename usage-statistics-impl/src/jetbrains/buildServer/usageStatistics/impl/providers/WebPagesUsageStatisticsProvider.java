/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.plugins.bean.ServerPluginInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.impl.GetRequestDetector;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class WebPagesUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements GetRequestDetector.Listener {
  @NotNull private static final Logger LOG = Logger.getLogger(WebPagesUsageStatisticsProvider.class);

  @NotNull private final List<Pattern> myPathPatterns = new ArrayList<Pattern>();
  @NotNull private final ServerPluginInfo myPluginDescriptor;
  @NotNull private final WebUsersProvider myWebUsersProvider;

  public WebPagesUsageStatisticsProvider(@NotNull final SBuildServer server,
                                         @NotNull final ServerPaths serverPaths,
                                         @NotNull final GetRequestDetector getRequestDetector,
                                         @NotNull final ServerPluginInfo pluginDescriptor,
                                         @NotNull final WebUsersProvider webUsersProvider) {
    super(server, serverPaths, new LinkedHashMap<Long, String>() {{
      put(Dates.ONE_WEEK, "Week");
      put(30 * Dates.ONE_DAY, "Month");
    }});
    myPluginDescriptor = pluginDescriptor;
    myWebUsersProvider = webUsersProvider;
    getRequestDetector.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.WEB_PAGES_USAGE;
  }

  @Used("spring")
  public void setConfigFilePath(@NotNull final String configFilePath) {
    readWebPagePatterns(configFilePath);
  }

  public void onGetRequest(@NotNull final HttpServletRequest request, @NotNull final SUser user) {
    String path = WebUtil.getPathWithoutContext(request);
    final String tab = request.getParameter("tab");
    if (tab != null) {
      path += "?tab=" + tab;
    }
    addUsage(path, user.getId());
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "webPagesUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "page";
  }

  @NotNull
  @Override
  protected String getToolIdName() {
    return "path";
  }

  @Override
  protected int getTotalUsersCount(@NotNull final Map<ICString, Set<ToolUsage>> usages, final long startDate) {
    return myWebUsersProvider.getWebUsers(startDate).size();
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String path) {
    for (final Pattern pathPattern : myPathPatterns) {
      if (pathPattern.matcher(path).matches()) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of web users)";
  }

  private void readWebPagePatterns(@NotNull final String configFilePath) {
    for (final File jarFile : myPluginDescriptor.getPluginJars()) {
      ZipFile zip = null;
      try {
        zip = new ZipFile(jarFile);
        final ZipEntry entry = zip.getEntry(configFilePath);
        if (entry == null) continue;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            try {
              myPathPatterns.add(Pattern.compile(line));
            } catch (final PatternSyntaxException e) {
              LOG.info("Invalid web page path pattern: " + line, e);
            }
          }
          break;
        }
        finally {
          reader.close();
        }
      } catch (final IOException e) {
        LOG.info(e.getLocalizedMessage(), e);
      } finally {
        try {
          if (zip != null) {
            zip.close();
          }
        } catch (final IOException e) {
          LOG.info(e.getLocalizedMessage(), e);
        }
      }
    }
  }
}
