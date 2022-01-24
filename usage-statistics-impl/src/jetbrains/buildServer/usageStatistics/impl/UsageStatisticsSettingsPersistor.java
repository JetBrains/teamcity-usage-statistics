/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.impl.FileWatcherFactory;
import jetbrains.buildServer.serverSide.impl.persisting.SettingsPersister;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsSettingsPersistor {

  @NotNull
  private static final Logger LOG = Logger.getInstance(UsageStatisticsSettingsPersistor.class.getName());
  @NotNull
  private static final String REPORTING_ENABLED = "reporting-enabled";

  @NotNull
  private final SettingsPersister mySettingsPersister;
  @NotNull
  private final FileWatcher myFileWatcher;
  @NotNull
  private final File myConfigFile;

  public UsageStatisticsSettingsPersistor(@NotNull ServerPaths serverPaths,
                                          @NotNull FileWatcherFactory fileWatcherFactory,
                                          @NotNull SettingsPersister settingsPersister) {
    myConfigFile = new File(serverPaths.getConfigDir(), "usage-statistics-config.xml");
    myFileWatcher = fileWatcherFactory.createFileWatcher(myConfigFile);
    myFileWatcher.registerListener(requestor -> loadSettings());
    mySettingsPersister = settingsPersister;
  }

  public void saveSettings(@NotNull UsageStatisticsSettings settings) {
    Element element = new Element("usage-statistics-settings");
    element.setAttribute(REPORTING_ENABLED, String.valueOf(settings.isReportingEnabled()));
    try {
      mySettingsPersister.scheduleSaveDocument("Save usage statistics settings", myFileWatcher, new Document(element));
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to save usage statistics settings into file \"" + myConfigFile.getAbsolutePath() + "\"", e);
    }
  }

  @NotNull
  public UsageStatisticsSettings loadSettings() {
    if (!myConfigFile.exists() || !myConfigFile.canRead()) {
      return new UsageStatisticsSettings();
    }

    Element element;
    try {
      element = FileUtil.parseDocument(myConfigFile);
    } catch (Exception e) {
      LOG.warnAndDebugDetails("Failed to load usage statistics settings from file \"" + myConfigFile.getAbsolutePath() + "\"", e);
      return new UsageStatisticsSettings();
    }

    String reportingEnabled = element.getAttributeValue(REPORTING_ENABLED);
    if (reportingEnabled == null) {
      return new UsageStatisticsSettings();
    }

    UsageStatisticsSettings settings = new UsageStatisticsSettings();
    settings.setReportingEnabled(Boolean.parseBoolean(reportingEnabled));

    return settings;
  }

}
