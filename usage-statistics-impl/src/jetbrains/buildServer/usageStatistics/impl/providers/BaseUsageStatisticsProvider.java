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

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import org.jetbrains.annotations.NotNull;

abstract class BaseUsageStatisticsProvider implements UsageStatisticsProvider {
  @NotNull protected final SBuildServer myServer;
  @NotNull protected final UsageStatisticsPresentationManager myPresentationManager;

  protected BaseUsageStatisticsProvider(@NotNull final SBuildServer server,
                                        @NotNull final UsageStatisticsPresentationManager presentationManager) {
    myServer = server;
    myPresentationManager = presentationManager;
    server.registerExtension(UsageStatisticsProvider.class, getClass().getName(), this);
  }
}
