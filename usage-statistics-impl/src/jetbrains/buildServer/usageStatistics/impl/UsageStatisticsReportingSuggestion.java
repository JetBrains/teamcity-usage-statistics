package jetbrains.buildServer.usageStatistics.impl;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.controllers.admin.AdminOverviewController.ITEM_PARAM_NAME;

/**
 * @author Maxim.Manuylov
 *         Date: 24.03.11
 */
public class UsageStatisticsReportingSuggestion extends SimplePageExtension {
  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCommonDataPersistor myDataPersistor;

  public UsageStatisticsReportingSuggestion(@NotNull final PagePlaces pagePlaces,
                                            @NotNull final PluginDescriptor pluginDescriptor,
                                            @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                            @NotNull final UsageStatisticsCommonDataPersistor dataPersistor) {
    super(pagePlaces);
    setPlaceId(PlaceId.ADMIN_BEFORE_CONTENT);
    setPluginName("usageStatisticsReportingSuggestion");
    setIncludeUrl(pluginDescriptor.getPluginResourcesPath("usageStatisticsReportingSuggestion.jsp"));
    register();

    mySettingsPersistor = settingsPersistor;
    myDataPersistor = dataPersistor;
  }

  @Override
  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    return request.getParameter(ITEM_PARAM_NAME) == null;
  }

  @Override
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    model.put("showSuggestion", !myDataPersistor.wasReportingSuggestionConsidered() && !mySettingsPersistor.loadSettings().isReportingEnabled());
  }
}
