
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jetbrains.buildServer.TeamCityCloud;
import jetbrains.buildServer.serverSide.NodeResponsibility;
import jetbrains.buildServer.serverSide.TeamCityNode;
import jetbrains.buildServer.serverSide.TeamCityNodes;
import jetbrains.buildServer.serverSide.TeamCityProperties;
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
    if (!TeamCityCloud.isCloud()) {
      publishClusterInfo(publisher, presentationManager);
    }
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.SERVER_CLUSTER;
  }

  private void publishClusterInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    int activityThresholdSeconds = TeamCityProperties.getInteger("teamcity.usageStatistics.nodeInactivityTime.seconds", 120);
    final List<TeamCityNode> onlineNodes = myTeamCityNodes.getOnlineNodes(activityThresholdSeconds);
    final Long secondaryNodesCount = onlineNodes.stream()
                                                .filter(TeamCityNode::isSecondaryNode)
                                                .count();
    final String id = makeId("secondaryNodes");
    presentationManager.applyPresentation(id, "Number of secondary nodes", myGroupName, null, null);
    publisher.publishStatistic(id, secondaryNodesCount);
    final Map<NodeResponsibility, Long> stats = onlineNodes.stream()
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