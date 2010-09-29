/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public class TriggerUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  public TriggerUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                        @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager, "Triggers Usage");
  }

  @NotNull
  @Override
  protected String getId() {
    return "triggerUsage";
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildType buildType : myServer.getProjectManager().getAllBuildTypes()) {
      for (final BuildTriggerDescriptor triggerDescriptor : buildType.getBuildTriggersCollection()) {
        final BuildTriggerService triggerService = triggerDescriptor.getBuildTriggerService();
        callback.addUsage(triggerService.getName(), triggerService.getDisplayName());
      }
    }
  }

  @NotNull
  @Override
  protected String prepareDisplayName(@NotNull final String extensionTypeDisplayName) {
    return extensionTypeDisplayName + " build configuration count";
  }

  @Override
  protected int getTotalCount() {
    return myServer.getProjectManager().getNumberOfBuildTypes();
  }
}
