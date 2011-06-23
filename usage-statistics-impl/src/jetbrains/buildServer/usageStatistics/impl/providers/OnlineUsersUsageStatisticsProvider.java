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

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

public class OnlineUsersUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;
  @NotNull private final WebUsersProvider myWebUsersProvider;
  @NotNull private final IDEUsersProvider myIDEUsersProvider;

  public OnlineUsersUsageStatisticsProvider(@NotNull final SBuildServer server,
                                            @NotNull final UsageStatisticsPresentationManager presentationManager,
                                            @NotNull final PluginDescriptor pluginDescriptor,
                                            @NotNull final WebUsersProvider webUsersProvider,
                                            @NotNull final IDEUsersProvider ideUsersProvider) {
    super(presentationManager, pluginDescriptor, createDWMPeriodDescriptions());
    myServer = server;
    myWebUsersProvider = webUsersProvider;
    myIDEUsersProvider = ideUsersProvider;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final String periodDescription, final long startDate) {
    final String webUsersId = makeId("webUsers", periodDescription);
    final String ideUsersId = makeId("ideUsers", periodDescription);

    final UsageStatisticsFormatter formatter = new PercentageFormatter(myServer.getUserModel().getNumberOfRegisteredUsers());
    myPresentationManager.applyPresentation(webUsersId, "Web users", myGroupName, formatter);
    myPresentationManager.applyPresentation(ideUsersId, "IDE users", myGroupName, formatter);

    publisher.publishStatistic(webUsersId, myWebUsersProvider.getWebUsersCount(startDate));
    publisher.publishStatistic(ideUsersId, myIDEUsersProvider.getIDEUsersCount(startDate));
  }

  @Override
  protected boolean mustSortStatistics() {
    return false;
  }
}
