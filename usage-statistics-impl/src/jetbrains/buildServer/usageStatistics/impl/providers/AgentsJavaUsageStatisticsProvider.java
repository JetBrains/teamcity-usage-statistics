
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.BuildAgentEx;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class AgentsJavaUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final BuildServerEx myServer;
  @NotNull private String myParameterName = "";

  public AgentsJavaUsageStatisticsProvider(@NotNull final BuildServerEx server) {
    myServer = server;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.AGENT_JAVA_VERSIONS;
  }

  public void setParameterName(@NotNull final String parameterName) {
    myParameterName = parameterName;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildAgent agent : getAuthorizedAgents()) {
      String javaVersion = agent.getConfigurationParameters().get(myParameterName);
      if (javaVersion == null) {
        javaVersion = "Unknown";
      }
      else {
        final int firstDotPos = javaVersion.indexOf('.');
        if (firstDotPos != -1) {
          final int secondDotPos = javaVersion.indexOf('.', firstDotPos + 1);
          if (secondDotPos != -1) {
            javaVersion = javaVersion.substring(0, secondDotPos);
          }
        }
      }
      callback.addUsage(javaVersion, javaVersion);
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