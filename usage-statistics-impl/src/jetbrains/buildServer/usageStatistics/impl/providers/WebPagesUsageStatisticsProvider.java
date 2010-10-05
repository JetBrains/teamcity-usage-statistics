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

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.BuildServerEx;
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
import org.jetbrains.annotations.NotNull;

public class WebPagesUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider {
  public WebPagesUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                         @NotNull final ServerPaths serverPaths,
                                         @NotNull final PagePlaces pagePlaces,
                                         @NotNull final PluginDescriptor pluginDescriptor,
                                         @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, serverPaths, presentationManager, new TreeMap<Long, String>() {{
      put(Dates.ONE_WEEK, "Week");
      put(30 * Dates.ONE_DAY, "Month");
    }});
    registerPageExtension(pagePlaces, pluginDescriptor);
  }

  public void processGetRequest(@NotNull final HttpServletRequest request) {
    final SUser user = SessionUser.getUser(request);
    if (user == null) return;
    final Object pageUrl = request.getAttribute("pageUrl");
    if (pageUrl != null && !(pageUrl instanceof String)) return;
    String path = WebUtil.getPathFromUrl(pageUrl == null
                                         ? WebUtil.getPathWithoutContext(request)
                                         : WebUtil.getPathWithoutContext(request, (String)pageUrl));
    if (!path.toLowerCase().endsWith(".html")) return;
    final String tab = request.getParameter("tab");
    if (tab != null) {
      path += "?tab=" + tab;
    }
    addUsage(path, user.getId());
  }

  @NotNull
  @Override
  protected String getId() {
    return "web";
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

  @NotNull
  @Override
  protected String prepareDisplayName(@NotNull final String toolId, @NotNull final String periodDescription) {
    return toolId;
  }

  @NotNull
  @Override
  protected String getGroupName(@NotNull final String periodDescription) {
    return "Web Pages Usage For The Last " + periodDescription;
  }

  private void registerPageExtension(@NotNull final PagePlaces pagePlaces, final PluginDescriptor pluginDescriptor) {
    final String pagePath = pluginDescriptor.getPluginResourcesPath("webPagesUsageStatistic.jsp");
    new SimplePageExtension(pagePlaces, PlaceId.ALL_PAGES_FOOTER, "webPagesUsageStatisticsProvider", pagePath) {
      {
        register();
      }

      @Override
      public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        super.fillModel(model, request);
        model.put("webPagesUsageStatisticsProvider", WebPagesUsageStatisticsProvider.this);
      }
    };
  }
}
