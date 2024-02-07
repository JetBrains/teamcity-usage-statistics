
package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.vcs.SVcsRoot;
import org.jetbrains.annotations.NotNull;

public class VCSUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;

  public VCSUsageStatisticsProvider(@NotNull final SBuildServer server) {
    myServer = server;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.VCS_ROOT_TYPES;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SVcsRoot vcsRoot : myServer.getVcsManager().getAllRegisteredVcsRoots()) {
      callback.addUsage(vcsRoot.getVcsName(), vcsRoot.getVcsDisplayName());
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "VCS root count (% of all VCS roots)";
  }
}