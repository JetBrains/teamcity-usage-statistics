
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class RunnerUsageStatisticsProvider extends BaseBuildTypeBasedExtensionUsageStatisticsProvider<SBuildRunnerDescriptor> {
  public RunnerUsageStatisticsProvider(@NotNull final SBuildServer server) {
    super(server);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.RUNNERS;
  }

  @NotNull
  @Override
  protected Collection<SBuildRunnerDescriptor> collectExtensions(@NotNull final SBuildType buildType) {
    return buildType.getResolvedSettings().getBuildRunners();
  }

  @NotNull
  @Override
  protected String getExtensionType(@NotNull final SBuildRunnerDescriptor runnerDescriptor) {
    return runnerDescriptor.getRunType().getType();
  }

  @NotNull
  @Override
  protected String getExtensionDisplayName(@NotNull final SBuildRunnerDescriptor runnerDescriptor, @NotNull final String extensionType) {
    return runnerDescriptor.getRunType().getDisplayName();
  }
}