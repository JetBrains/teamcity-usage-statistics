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

import java.util.*;
import jetbrains.buildServer.serverSide.BuildAgentManagerEx;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import org.jetbrains.annotations.NotNull;

public class AgentsJavaUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "Agent Java Versions (agents)";
  @NotNull private static final Comparator<String> IC_COMPARATOR = new Comparator<String>() {
    public int compare(final String s1, final String s2) {
      return s1.compareToIgnoreCase(s2);
    }
  };

  public AgentsJavaUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                           @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final BuildAgentManagerEx buildAgentManager = (BuildAgentManagerEx)myServer.getBuildAgentManager();
    final List<SBuildAgent> agents = new ArrayList<SBuildAgent>();
    agents.addAll(buildAgentManager.getRegisteredAgents(true));
    agents.addAll(buildAgentManager.getUnregisteredAgents(true));
    final Map<String, Integer> javaCounters = new TreeMap<String, Integer>(IC_COMPARATOR);
    for (final SBuildAgent agent : agents) {
      String javaVersion = agent.getConfigurationParameters().get("teamcity.agent.jvm.version");
      if (javaVersion == null) {
        javaVersion = "Unknown";
      }
      if (javaCounters.containsKey(javaVersion)) {
        javaCounters.put(javaVersion, javaCounters.get(javaVersion) + 1);
      }
      else {
        javaCounters.put(javaVersion, 1);
      }
    }
    final int agentsNumber = agents.size();
    for (final Map.Entry<String, Integer> entry : javaCounters.entrySet()) {
      final String javaVersion = entry.getKey();
      final String id = String.format("jb.agent.java[%s]", javaVersion);
      myPresentationManager.applyPresentation(id, javaVersion, ourGroupName, new PercentageFormatter(agentsNumber));
      publisher.publishStatistic(id, entry.getValue());
    }
  }
}
