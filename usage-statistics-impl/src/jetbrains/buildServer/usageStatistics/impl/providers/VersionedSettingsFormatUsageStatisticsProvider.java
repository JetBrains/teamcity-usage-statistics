/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.impl.versionedSettings.VersionedSettingsConfig;
import jetbrains.buildServer.serverSide.versionedSettings.VersionedSettingsManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class VersionedSettingsFormatUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {

  private final ProjectManager myProjectManager;
  private final VersionedSettingsManager myVersionedSettingsManager;

  public VersionedSettingsFormatUsageStatisticsProvider(@NotNull ProjectManager projectManager,
                                                        @NotNull VersionedSettingsManager versionedSettingsManager) {
    myProjectManager = projectManager;
    myVersionedSettingsManager = versionedSettingsManager;
  }


  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (SProject p : myProjectManager.getProjects()) {
      VersionedSettingsConfig rawConfig = myVersionedSettingsManager.readConfig(p);
      if (rawConfig.isEnabled() && !rawConfig.isSameSettingsAsParent()) {
        String format = rawConfig.getGenerator();
        callback.addUsage(format, format);
      }
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Projects count (% of projects with versioned settings enabled)";
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.VCS_FEATURES;
  }
}
