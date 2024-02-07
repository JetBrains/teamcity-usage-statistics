
package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.versionedSettings.VersionedSettingsManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.vcs.SVcsRoot;
import org.jetbrains.annotations.NotNull;

public class VersionedSettingsUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {

  private final ProjectManager myProjectManager;
  private final VersionedSettingsManager myVersionedSettingsManager;

  public VersionedSettingsUsageStatisticsProvider(@NotNull ProjectManager projectManager,
                                                  @NotNull VersionedSettingsManager versionedSettingsManager) {
    myProjectManager = projectManager;
    myVersionedSettingsManager = versionedSettingsManager;
  }

  @Override
  protected void collectUsages(@NotNull UsagesCollectorCallback callback) {
    for (SProject p : myProjectManager.getProjects()) {
      SVcsRoot root = myVersionedSettingsManager.getOwnVersionedSettingsVcsRoot(p);
      if (root != null)
        callback.addUsage(root.getVcsName(), root.getVcsDisplayName());
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