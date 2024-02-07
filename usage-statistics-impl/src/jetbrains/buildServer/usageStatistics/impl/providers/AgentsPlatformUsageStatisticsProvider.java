
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.BuildAgentEx;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class AgentsPlatformUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final BuildServerEx myServer;

  public AgentsPlatformUsageStatisticsProvider(@NotNull final BuildServerEx server) {
    myServer = server;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.AGENT_PLATFORMS;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildAgent agent : getAuthorizedAgents()) {
      final Map<String, String> parameters = agent.getConfigurationParameters();
      final String name = parameters.get("teamcity.agent.jvm.os.name");
      final String version = parameters.get("teamcity.agent.jvm.os.version");
      if (name == null) {
        callback.addUsage("Unknown", "Unknown");
        continue;
      }
      final StringBuilder data = new StringBuilder(name);
      final StringBuilder presentation = new StringBuilder(name);
      if (version != null) {
        data.append('|').append(version);
        presentation.append(' ').append(version);
      }
      callback.addUsage(data.toString(), presentation.toString());
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Agent count (% of all authorized agents)";
  }

  @Override
  protected int getTotalUsagesCount(@NotNull final Map<ExtensionType, Integer> extensionUsages) {
    return getAuthorizedAgents().size();
  }

  @NotNull
  private List<BuildAgentEx> getAuthorizedAgents() {
    return myServer.getBuildAgentManager().getAllAgents(false);
  }
}