/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import java.util.HashSet;
import java.util.Set;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public class RunnerUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;

  public RunnerUsageStatisticsProvider(@NotNull final SBuildServer server, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(presentationManager);
    myServer = server;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      final Set<String> collectedTypes = new HashSet<String>();
      for (final SBuildRunnerDescriptor runnerDescriptor : buildType.getBuildRunners()) {
        final RunType runType = runnerDescriptor.getRunType();
        final String type = runType.getType();
        if (!collectedTypes.contains(type)) {
          callback.addUsage(type, runType.getDisplayName());
          collectedTypes.add(type);
        }
      }
    }
  }
}
