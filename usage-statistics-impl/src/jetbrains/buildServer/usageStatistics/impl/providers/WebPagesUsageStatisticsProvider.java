/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.plugins.bean.ServerPluginInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WebPagesUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements WebUsersProvider {
  @NotNull private static final Logger LOG = Logger.getLogger(WebPagesUsageStatisticsProvider.class);

  @NotNull private final List<Pattern> myPathPatterns = new ArrayList<Pattern>();
  @NotNull private final ServerPluginInfo myPluginDescriptor;

  public WebPagesUsageStatisticsProvider(@NotNull final SBuildServer server,
                                         @NotNull final ServerPaths serverPaths,
                                         @NotNull final PagePlaces pagePlaces,
                                         @NotNull final PluginDescriptor pluginDescriptor,
                                         @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, serverPaths, presentationManager, new LinkedHashMap<Long, String>() {{
      put(Dates.ONE_WEEK, "Week");
      put(30 * Dates.ONE_DAY, "Month");
    }}, pluginDescriptor);
    myPluginDescriptor = (ServerPluginInfo) pluginDescriptor;
    registerPageExtension(pagePlaces, pluginDescriptor);
  }

  public void setConfigFilePath(@NotNull final String configFilePath) {
    readWebPagePatterns(configFilePath);
  }

  @NotNull
  public Set<String> getWebUsers(final long fromTimestamp) {
    return getUsers(fromTimestamp);
  }

  public void processGetRequest(@NotNull final HttpServletRequest request) {
    final SUser user = SessionUser.getUser(request);
    if (user == null) return;
    String path = WebUtil.getPathFromUrl(WebUtil.getPathWithoutContext(request));
    if (!path.toLowerCase().endsWith(".html")) return;
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

  private void registerPageExtension(@NotNull final PagePlaces pagePlaces, final PluginDescriptor pluginDescriptor) {
    final String pagePath = pluginDescriptor.getPluginResourcesPath("empty.jsp");
    new SimplePageExtension(pagePlaces, PlaceId.ALL_PAGES_FOOTER, "webPagesUsageStatisticsProvider", pagePath) {
      {
        register();
      }

      @Override
      public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        super.fillModel(model, request);
        if (isGet(request)) {
          processGetRequest(request);
        }
      }
    };
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
