/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import java.io.File;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.util.XmlUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsSettingsPersistor {
  @NotNull private static final String REPORTING_ENABLED = "reporting-enabled";
  @NotNull private final File myConfigFile;

  public UsageStatisticsSettingsPersistor(@NotNull final ServerPaths serverPaths) {
    myConfigFile = new File(serverPaths.getConfigDir(), "usage-statistics-config.xml");
  }

  public void saveSettings(@NotNull final UsageStatisticsSettings settings) {
    final Element element = new Element("usage-statistics-settings");
    element.setAttribute(REPORTING_ENABLED, String.valueOf(settings.isReportingEnabled()));
    XmlUtil.saveXml(element, myConfigFile);
  }

  @NotNull
  public UsageStatisticsSettings loadSettings() {
    final UsageStatisticsSettings settings = new UsageStatisticsSettings();

    final Element element = XmlUtil.loadXml(myConfigFile);
    if (element != null) {
      final String reportingEnabled = element.getAttributeValue(REPORTING_ENABLED);
      if (reportingEnabled != null) {
        settings.setReportingEnabled(Boolean.parseBoolean(reportingEnabled));
      }
    }

    return settings;
  }
}
