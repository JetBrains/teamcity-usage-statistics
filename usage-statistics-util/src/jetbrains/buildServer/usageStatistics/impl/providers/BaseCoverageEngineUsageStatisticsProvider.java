
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCoverageEngineUsageStatisticsProvider extends BaseBuildTypeBasedExtensionUsageStatisticsProvider<SBuildRunnerDescriptor> {
  @NotNull private final Map<String, String> myEngineName2DisplayName = new HashMap<String, String>();

  protected BaseCoverageEngineUsageStatisticsProvider(@NotNull final SBuildServer server) {
    super(server);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.COVERAGE_ENGINES;
  }

  protected void registerCoverageEngine(@NotNull final String engineName, @NotNull final String engineDisplayName) {
    myEngineName2DisplayName.put(engineName, engineDisplayName);
  }

  @Nullable
  protected abstract String getSelectedEngineName(@NotNull Map<String, String> parameters);

  @NotNull
  @Override
  protected Collection<SBuildRunnerDescriptor> collectExtensions(@NotNull final SBuildType buildType) {
    return buildType.getResolvedSettings().getBuildRunners();
  }

  @Nullable
  @Override
  protected String getExtensionType(@NotNull final SBuildRunnerDescriptor runnerDescriptor) {
    return getSelectedEngineName(runnerDescriptor.getParameters());
  }

  @NotNull
  @Override
  protected String getExtensionDisplayName(@NotNull final SBuildRunnerDescriptor runnerDescriptor, @NotNull final String extensionType) {
    return myEngineName2DisplayName.get(extensionType);
  }
}