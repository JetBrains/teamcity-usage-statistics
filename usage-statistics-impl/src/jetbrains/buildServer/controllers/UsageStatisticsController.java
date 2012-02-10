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

package jetbrains.buildServer.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.audit.ActionType;
import jetbrains.buildServer.serverSide.audit.AuditLog;
import jetbrains.buildServer.serverSide.audit.AuditLogFactory;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.impl.auth.ServerAuthUtil;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsCommonDataPersistor;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettings;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.*;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

public class UsageStatisticsController extends BaseFormXmlController {
  @NotNull public static final String USAGE_STATISTICS_ITEM_VALUE = "usageStatistics";
  @NotNull private static final String USAGE_STATISTICS_REPORTING_STATUS_MESSAGE_KEY = "usageStatisticsReportingStatusMessage";

  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCommonDataPersistor myDataPersistor;
  @NotNull private final UsageStatisticsCollector myStatisticsCollector;
  @NotNull private final UsageStatisticsPresentationManagerEx myPresentationManager;
  @NotNull private final AuditLog myAuditLog;
  @NotNull private final String myJspPagePath;

  @Used("spring")
  public UsageStatisticsController(@NotNull final SBuildServer server,
                                   @NotNull final AuthorizationInterceptor authInterceptor,
                                   @NotNull final WebControllerManager webControllerManager,
                                   @NotNull final PluginDescriptor pluginDescriptor,
                                   @NotNull final PagePlaces pagePlaces,
                                   @NotNull final AuditLogFactory auditLogFactory,
                                   @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                   @NotNull final UsageStatisticsCommonDataPersistor dataPersistor,
                                   @NotNull final UsageStatisticsCollector statisticsCollector,
                                   @NotNull final UsageStatisticsPresentationManagerEx presentationManager) {
    super(server);
    mySettingsPersistor = settingsPersistor;
    myDataPersistor = dataPersistor;
    myStatisticsCollector = statisticsCollector;
    myPresentationManager = presentationManager;
    myAuditLog = auditLogFactory.createForServer();
    myJspPagePath = pluginDescriptor.getPluginResourcesPath("usageStatistics.jsp");

    UsageStatisticsControllerUtil.register(this, authInterceptor, webControllerManager, "/admin/usageStatistics.html");

    final SimpleCustomTab tab = new AdminPage(pagePlaces) {
      @Override
      public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return super.isAvailable(request) && checkHasGlobalPermissions(request, Permission.VIEW_USAGE_STATISTICS);
      }

      @NotNull
      public String getGroup() {
        return SERVER_RELATED_GROUP;
      }
    };
    tab.addCssFile(pluginDescriptor.getPluginResourcesPath("css/usageStatistics.css"));
    tab.addCssFile("/css/settingsBlock.css");
    tab.addJsFile(pluginDescriptor.getPluginResourcesPath("js/usageStatistics.js"));
    tab.setPluginName(USAGE_STATISTICS_ITEM_VALUE);
    tab.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("usageStatisticsTab.jsp"));
    tab.setTabTitle("Usage Statistics");
    tab.setPosition(PositionConstraint.after("license"));
    tab.register();
  }

  @Override
  protected ModelAndView doGet(final HttpServletRequest request, final HttpServletResponse response) {
    final ModelAndView modelAndView = new ModelAndView(myJspPagePath);
    //noinspection unchecked
    modelAndView.getModel().put("statisticsData", new UsageStatisticsBean(mySettingsPersistor, myStatisticsCollector, myPresentationManager));
    return modelAndView;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response, final Element xmlResponse) {
    if (request.getParameter("forceCollectingNow") != null) {
      myStatisticsCollector.forceAsynchronousCollectingNow();
      myAuditLog.logUserAction(ActionType.USAGE_STATISTICS_COLLECTING_STARTED, null, null);
      return;
    }

    final String reportingEnabledStr = request.getParameter("reportingEnabled");
    if (reportingEnabledStr != null) {
      checkCanChangeServerSettings(request);
      myDataPersistor.markReportingSuggestionAsConsidered();
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

  private void checkCanChangeServerSettings(@NotNull final HttpServletRequest request) {
    final SUser user = SessionUser.getUser(request);
    if (user == null) {
      throw new IllegalStateException("Session is expired or user is not specified");
    }
    ServerAuthUtil.checkHasGlobalPermission(user, Permission.CHANGE_SERVER_SETTINGS);
  }

  private void setReportingEnabled(final boolean reportingEnabled) {
    final UsageStatisticsSettings settings = mySettingsPersistor.loadSettings();
    settings.setReportingEnabled(reportingEnabled);
    mySettingsPersistor.saveSettings(settings);
    myAuditLog.logUserAction(reportingEnabled ? ActionType.USAGE_STATISTICS_REPORTING_ENABLED : ActionType.USAGE_STATISTICS_REPORTING_DISABLED, null, null);
  }
}
