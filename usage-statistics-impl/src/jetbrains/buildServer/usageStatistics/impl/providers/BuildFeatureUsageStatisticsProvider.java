
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.ArrayList;
import java.util.Collection;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.filters.Filter;
import jetbrains.buildServer.util.filters.FilterUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class BuildFeatureUsageStatisticsProvider extends BaseBuildTypeBasedExtensionUsageStatisticsProvider<SBuildFeatureDescriptor> {
  public BuildFeatureUsageStatisticsProvider(@NotNull final SBuildServer server) {
    super(server);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.BUILD_FEATURES;
  }

  @NotNull
  @Override
  protected Collection<SBuildFeatureDescriptor> collectExtensions(@NotNull final SBuildType buildType) {
    return FilterUtil.filterAndCopy(buildType.getBuildFeatures(), new ArrayList<SBuildFeatureDescriptor>(), new Filter<SBuildFeatureDescriptor>() {
      public boolean accept(@NotNull final SBuildFeatureDescriptor featureDescriptor) {
        return featureDescriptor.getBuildFeature().getPlaceToShow() == BuildFeature.PlaceToShow.GENERAL;
      }
    });
  }

  @NotNull
  @Override
  protected String getExtensionType(@NotNull final SBuildFeatureDescriptor featureDescriptor) {
    return featureDescriptor.getBuildFeature().getType();
  }

  @NotNull
  @Override
  protected String getExtensionDisplayName(@NotNull final SBuildFeatureDescriptor featureDescriptor, @NotNull final String extensionType) {
    return featureDescriptor.getBuildFeature().getDisplayName();
  }
}