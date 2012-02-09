package jetbrains.buildServer.usageStatistics.impl;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.controllers.admin.AdminBeforeContentExtension;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

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
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    model.put("showSuggestion", !myDataPersistor.wasReportingSuggestionConsidered() && !mySettingsPersistor.loadSettings().isReportingEnabled());
  }
}
