/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
