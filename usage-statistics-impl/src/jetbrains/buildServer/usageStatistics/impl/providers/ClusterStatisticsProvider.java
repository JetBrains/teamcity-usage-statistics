/*
 * Copyright 2000-2019 JetBrains s.r.o.
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

import java.util.Map;
import java.util.stream.Collectors;
import jetbrains.buildServer.serverSide.NodeResponsibility;
import jetbrains.buildServer.serverSide.TeamCityNode;
import jetbrains.buildServer.serverSide.TeamCityNodes;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

import static java.util.function.Function.identity;

public class ClusterStatisticsProvider extends BaseDefaultUsageStatisticsProvider  {

  @NotNull private final TeamCityNodes myTeamCityNodes;


  public ClusterStatisticsProvider(@NotNull final TeamCityNodes teamCityNodes) {
    myTeamCityNodes = teamCityNodes;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    publishClusterInfo(publisher, presentationManager);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.SERVER_CLUSTER;
  }

  private void publishClusterInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final Long secondaryNodesCount = myTeamCityNodes.getNodes().stream()
                                                    .filter(TeamCityNode::isSecondaryNode)
                                                    .count();
    final String id = makeId("secondaryNodes");
    presentationManager.applyPresentation(id, "Number of secondary nodes", myGroupName, null, null);
    publisher.publishStatistic(id, secondaryNodesCount);
    final Map<NodeResponsibility, Long> stats = myTeamCityNodes.getOnlineNodes().stream()
                                                               .filter(TeamCityNode::isSecondaryNode)
                                                               .map(TeamCityNode::getEnabledResponsibilities)
                                                               .flatMap(it -> it.stream())
                                                               .collect(Collectors.groupingBy(identity(), Collectors.counting()));
    stats.forEach((nodeResponsibility, count) -> {
      final String rId = makeId("nodeResponsibility." + nodeResponsibility.name().toLowerCase());
      presentationManager.applyPresentation(rId, nodeResponsibility.getDisplayName(), myGroupName, null, null);
      publisher.publishStatistic(rId, count);
    });
  }
}
