/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
