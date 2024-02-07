
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class TriggerUsageStatisticsProvider extends BaseBuildTypeBasedExtensionUsageStatisticsProvider<BuildTriggerDescriptor> {
  public TriggerUsageStatisticsProvider(@NotNull final SBuildServer server) {
    super(server);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.TRIGGERS;
  }

  @NotNull
  @Override
  protected Collection<BuildTriggerDescriptor> collectExtensions(@NotNull final SBuildType buildType) {
    return buildType.getBuildTriggersCollection();
  }

  @NotNull
  @Override
  protected String getExtensionType(@NotNull final BuildTriggerDescriptor triggerDescriptor) {
    return triggerDescriptor.getBuildTriggerService().getName();
  }

  @NotNull
  @Override
  protected String getExtensionDisplayName(@NotNull final BuildTriggerDescriptor triggerDescriptor, @NotNull final String extensionType) {
    return triggerDescriptor.getBuildTriggerService().getDisplayName();
  }
}