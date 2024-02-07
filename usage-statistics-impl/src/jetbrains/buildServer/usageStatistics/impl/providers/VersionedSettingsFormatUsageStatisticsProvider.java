
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
        String format = rawConfig.getFormat();
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