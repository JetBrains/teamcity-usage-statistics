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
