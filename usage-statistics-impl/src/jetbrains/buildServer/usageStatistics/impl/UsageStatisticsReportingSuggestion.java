
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