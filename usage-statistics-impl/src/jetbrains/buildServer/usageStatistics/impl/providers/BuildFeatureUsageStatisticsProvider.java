/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
