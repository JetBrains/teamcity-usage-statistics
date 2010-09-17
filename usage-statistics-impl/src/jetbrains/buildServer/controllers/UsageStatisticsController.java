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

package jetbrains.buildServer.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettings;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.web.openapi.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

public class UsageStatisticsController extends BaseFormXmlController {
  @NotNull private static final String USAGE_STATISTICS_REPORTING_STATUS_MESSAGE_KEY = "usageStatisticsReportingStatusMessage";

  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCollector myStatisticsCollector;
  @NotNull private final UsageStatisticsPresentationManagerEx myPresentationManager;
  @NotNull private final String myJspPath;

  @Used("spring")
  public UsageStatisticsController(@NotNull final SBuildServer server,
                                   @NotNull final AuthorizationInterceptor authInterceptor,
                                   @NotNull final WebControllerManager webControllerManager,
                                   @NotNull final PluginDescriptor pluginDescriptor,
                                   @NotNull final PagePlaces pagePlaces,
                                   @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                   @NotNull final UsageStatisticsCollector statisticsCollector,
                                   @NotNull final UsageStatisticsPresentationManagerEx presentationManager) {
    super(server);
    mySettingsPersistor = settingsPersistor;
    myStatisticsCollector = statisticsCollector;
    myPresentationManager = presentationManager;
    myJspPath = pluginDescriptor.getPluginResourcesPath("usageStatistics.jsp");

    authInterceptor.addPathBasedPermissionsChecker("/admin/usageStatistics.html", new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull final AuthorityHolder authorityHolder, @NotNull final HttpServletRequest request) throws AccessDeniedException {
        if (!authorityHolder.isPermissionGrantedGlobally(Permission.VIEW_USAGE_STATISTICS)) {
          throw new AccessDeniedException(authorityHolder, "You do not have enough permissions to view usage statistics");
        }
      }
    });

    final SimpleCustomTab tab = new SimpleCustomTab(pagePlaces);
    tab.setPlaceId(PlaceId.ADMIN_SERVER_CONFIGURATION_TAB);
    tab.setPluginName("usage-statistics");
    tab.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("usageStatisticsTab.jsp"));
    tab.setTabTitle("Usage Statistics");
    tab.setPosition(PositionConstraint.last());
    tab.register();

    webControllerManager.registerController("/admin/usageStatistics.html", this);
  }

  @Override
  protected ModelAndView doGet(final HttpServletRequest request, final HttpServletResponse response) {
    final ModelAndView modelAndView = new ModelAndView(myJspPath);
    //noinspection unchecked
    modelAndView.getModel().put("statisticsData", new UsageStatisticsBean(mySettingsPersistor, myStatisticsCollector, myPresentationManager));
    return modelAndView;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response, final Element xmlResponse) {
    final String reportingEnabledStr = request.getParameter("reportingEnabled");
    if (reportingEnabledStr != null) {
      final boolean reportingEnabled = "true".equalsIgnoreCase(reportingEnabledStr);
      try {
        setReportingEnabled(reportingEnabled);
        ActionMessages.getOrCreateMessages(request).addMessage(
          USAGE_STATISTICS_REPORTING_STATUS_MESSAGE_KEY,
          reportingEnabled ? "Usage statistics will be sent to JetBrains periodically" : "Usage statistics will not be sent to JetBrains"
        );
      }
      catch (final Throwable e) {
        Loggers.SERVER.error("Cannot update statistics reporting status: ", e);
        ActionMessages.getOrCreateMessages(request).addMessage(USAGE_STATISTICS_REPORTING_STATUS_MESSAGE_KEY, "Internal server error occurred. Please try again later.");
        xmlResponse.addContent("error");
      }
    }
  }

  private void setReportingEnabled(final boolean reportingEnabled) {
    final UsageStatisticsSettings settings = mySettingsPersistor.loadSettings();
    settings.setReportingEnabled(reportingEnabled);
    mySettingsPersistor.saveSettings(settings);
  }
}
