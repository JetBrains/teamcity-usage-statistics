/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.controllers.license.LicenseAgreementDispatcher;
import jetbrains.buildServer.controllers.license.LicenseAgreementListener;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 24.03.11
 */
public class UsageStatisticsLicenseAgreementListener implements LicenseAgreementListener {
  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsCommonDataPersistor myDataPersistor;

  public UsageStatisticsLicenseAgreementListener(@NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                                 @NotNull final PagePlaces pagePlaces,
                                                 @NotNull final PluginDescriptor pluginDescriptor,
                                                 @NotNull final LicenseAgreementDispatcher dispatcher,
                                                 @NotNull final UsageStatisticsCommonDataPersistor dataPersistor) {
    mySettingsPersistor = settingsPersistor;
    myDataPersistor = dataPersistor;
    registerPageExtension(pagePlaces, pluginDescriptor);
    dispatcher.addListener(this);
  }

  public void onLicenseAccepted(@NotNull final HttpServletRequest request) {
    myDataPersistor.markReportingSuggestionAsConsidered();
    if (Boolean.parseBoolean(request.getParameter("sendUsageStatistics"))) {
      final UsageStatisticsSettings settings = mySettingsPersistor.loadSettings();
      settings.setReportingEnabled(true);
      mySettingsPersistor.saveSettings(settings);
    }
  }

  private void registerPageExtension(@NotNull final PagePlaces pagePlaces, @NotNull final PluginDescriptor pluginDescriptor) {
    final String pagePath = pluginDescriptor.getPluginResourcesPath("sendUsageStatisticsSetting.jsp");
    new SimplePageExtension(pagePlaces, PlaceId.ACCEPT_LICENSE_SETTING, "usageStatisticsLicenseSetting", pagePath).register();
  }
}
