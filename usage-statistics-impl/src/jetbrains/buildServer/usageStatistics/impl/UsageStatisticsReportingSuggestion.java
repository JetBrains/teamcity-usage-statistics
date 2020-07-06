/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.TeamCityCloud;
import jetbrains.buildServer.controllers.admin.AdminBeforeContentExtension;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.serverSide.auth.AuthUtil.hasGlobalPermission;
import static jetbrains.buildServer.serverSide.auth.AuthUtil.hasPermissionToManageProject;
import static jetbrains.buildServer.serverSide.auth.Permission.CHANGE_SERVER_SETTINGS;
import static jetbrains.buildServer.serverSide.auth.Permission.VIEW_USAGE_STATISTICS;

/**
 * @author Maxim.Manuylov
 *         Date: 24.03.11
 */
public class UsageStatisticsReportingSuggestion extends AdminBeforeContentExtension {
  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCommonDataPersistor myDataPersistor;

  public UsageStatisticsReportingSuggestion(@NotNull final PagePlaces pagePlaces,
                                            @NotNull final PluginDescriptor pluginDescriptor,
                                            @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                            @NotNull final UsageStatisticsCommonDataPersistor dataPersistor) {
    super(pagePlaces);
    setPluginName("usageStatisticsReportingSuggestion");
    setIncludeUrl(pluginDescriptor.getPluginResourcesPath("usageStatisticsReportingSuggestion.jsp"));
    register();

    mySettingsPersistor = settingsPersistor;
    myDataPersistor = dataPersistor;
  }

  @Override
  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    return super.isAvailable(request) && hasNeededPermissions(request);
  }

  private boolean hasNeededPermissions(@NotNull final HttpServletRequest request) {
    final SUser user = SessionUser.getUser(request);
    return user != null &&
           hasGlobalPermission(user, VIEW_USAGE_STATISTICS) &&
           (hasGlobalPermission(user, CHANGE_SERVER_SETTINGS) || TeamCityCloud.isCloud() && hasPermissionToManageProject(user, BuildProject.ROOT_PROJECT_ID));
  }

  @Override
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    model.put("showSuggestion", !myDataPersistor.wasReportingSuggestionConsidered() && !mySettingsPersistor.loadSettings().isReportingEnabled());
  }
}
