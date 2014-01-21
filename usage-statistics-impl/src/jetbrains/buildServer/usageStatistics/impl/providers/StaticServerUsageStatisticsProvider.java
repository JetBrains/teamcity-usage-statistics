/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.clouds.server.CloudStatisticsProvider;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManagerEx;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {
  @NotNull private final BuildServerEx myServer;
  @NotNull private final UserGroupManager myUserGroupManager;
  @NotNull private final AgentPoolManager myAgentPoolManager;
  @NotNull private final CloudStatisticsProvider myCloudProvider;

  public StaticServerUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                             @NotNull final UserGroupManager userGroupManager,
                                             @NotNull final AgentPoolManager agentPoolManager,
                                             @NotNull final CloudStatisticsProvider cloudProvider) {
    myServer = server;
    myUserGroupManager = userGroupManager;
    myAgentPoolManager = agentPoolManager;
    myCloudProvider = cloudProvider;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.GENERAL;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    publishNumberOfAgents(publisher, presentationManager);
    publishNumberOfVirtualAgents(publisher, presentationManager);
    publishNumberOfAgentPools(publisher, presentationManager);

    publishNumberOfBuildTypes(publisher, presentationManager);
    publishNumberOfDependencies(publisher, presentationManager);
    publishNumberOfProjects(publisher, presentationManager);
    publishNumberOfUserGroups(publisher, presentationManager);
    publishNumberOfUsers(publisher, presentationManager);
    publishNumberOfVcsRoots(publisher, presentationManager);

    publishNumberOfCloudImages(publisher, presentationManager);
    publishNumberOfCloudProfiles(publisher, presentationManager);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String allRegisteredAgentNumberId = makeId("allRegisteredAgentNumber");
    final String authorizedRegisteredAgentNumberId = makeId("authorizedRegisteredAgentNumber");

    final BuildAgentManagerEx buildAgentManager = myServer.getBuildAgentManager();
    final int allRegisteredAgentsNumber = buildAgentManager.getRegisteredAgents(true).size();

    presentationManager.applyPresentation(allRegisteredAgentNumberId, "Connected agents (all)", myGroupName, null, null);
    publisher.publishStatistic(allRegisteredAgentNumberId, allRegisteredAgentsNumber);

    presentationManager.applyPresentation(authorizedRegisteredAgentNumberId, "Connected agents (authorized only)", myGroupName, new PercentageFormatter(allRegisteredAgentsNumber), "Agent count (% of all connected agents)");
    publisher.publishStatistic(authorizedRegisteredAgentNumberId, buildAgentManager.getRegisteredAgents(false).size());
  }

  private void publishNumberOfVirtualAgents(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String virtualAgentNumberId = makeId("virtualAgentNumber");
    presentationManager.applyPresentation(virtualAgentNumberId, "Virtual agents", myGroupName, null, null);
    publisher.publishStatistic(virtualAgentNumberId, myCloudProvider.getNumberOfRunningInstances());
  }

  private void publishNumberOfAgentPools(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String agentPoolsId = makeId("agentPools");
    presentationManager.applyPresentation(agentPoolsId, "Agent pools", myGroupName, null, null);
    publisher.publishStatistic(agentPoolsId, myAgentPoolManager.getAllAgentPools().size());
  }

  private void publishNumberOfCloudProfiles(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String cloudProfilesId = makeId("cloudProfiles");
    presentationManager.applyPresentation(cloudProfilesId, "Cloud profiles", myGroupName, null, null);
    publisher.publishStatistic(cloudProfilesId, myCloudProvider.getNumberOfProfiles());
  }

  private void publishNumberOfCloudImages(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String cloudImagesId = makeId("cloudImages");
    presentationManager.applyPresentation(cloudImagesId, "Cloud images", myGroupName, null, null);
    publisher.publishStatistic(cloudImagesId, myCloudProvider.getNumberOfImages());
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String buildTypeNumberId = makeId("buildTypeNumber");
    final String activeBuildTypeNumberId = makeId("activeBuildTypeNumber");
    final String multiVcsRootBuildTypeNumberId = makeId("multiVcsRootBuildTypeNumber");

    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();

    presentationManager.applyPresentation(buildTypeNumberId, "Build configurations", myGroupName, null, null);
    publisher.publishStatistic(buildTypeNumberId, buildTypeNumber);

    final List<SBuildType> activeBuildTypes = myServer.getProjectManager().getActiveBuildTypes();
    final int activeBuildTypeNumber = activeBuildTypes.size();

    presentationManager.applyPresentation(activeBuildTypeNumberId, "Active build configurations", myGroupName, new PercentageFormatter(buildTypeNumber), "Build configuration count (% of all build configurations)");
    publisher.publishStatistic(activeBuildTypeNumberId, activeBuildTypeNumber);

    int multiVcsRootBuildTypeNumber = 0;
    for (SBuildType buildType : activeBuildTypes) {
      if (buildType.getVcsRoots().size() > 1) {
        multiVcsRootBuildTypeNumber++;
      }
    }

    presentationManager.applyPresentation(multiVcsRootBuildTypeNumberId, "Active build configurations with several VCS roots", myGroupName, new PercentageFormatter(activeBuildTypeNumber), "Build configuration count (% of all active build configurations)");
    publisher.publishStatistic(multiVcsRootBuildTypeNumberId, multiVcsRootBuildTypeNumber);
  }

  private void publishNumberOfDependencies(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String snapshotDependencyNumberId = makeId("snapshotDependencyNumber");
    final String artifactDependencyNumberId = makeId("artifactDependencyNumber");

    int snapshotDependencies = 0, artifactDependencies = 0;
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      snapshotDependencies += buildType.getDependencies().size();
      artifactDependencies += buildType.getArtifactDependencies().size();
    }

    presentationManager.applyPresentation(snapshotDependencyNumberId, "Snapshot dependencies", myGroupName, null, null);
    publisher.publishStatistic(snapshotDependencyNumberId, snapshotDependencies);

    presentationManager.applyPresentation(artifactDependencyNumberId, "Artifact dependencies", myGroupName, null, null);
    publisher.publishStatistic(artifactDependencyNumberId, artifactDependencies);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String projectNumberId = makeId("projectNumber");
    final String archivedProjectNumberId = makeId("archivedProjectNumber");

    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();

    presentationManager.applyPresentation(projectNumberId, "Projects", myGroupName, null, null);
    publisher.publishStatistic(projectNumberId, projectNumber);

    presentationManager.applyPresentation(archivedProjectNumberId, "Archived projects", myGroupName, new PercentageFormatter(projectNumber), "Project count (% of all projects)");
    publisher.publishStatistic(archivedProjectNumberId, myServer.getProjectManager().getArchivedProjects().size());
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String userGroupNumberId = makeId("userGroupNumber");
    presentationManager.applyPresentation(userGroupNumberId, "User groups", myGroupName, null, null);
    publisher.publishStatistic(userGroupNumberId, myUserGroupManager.getUserGroups().size());
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String userNumberId = makeId("userNumber");
    presentationManager.applyPresentation(userNumberId, "Users", myGroupName, null, null);
    publisher.publishStatistic(userNumberId, myServer.getUserModel().getNumberOfRegisteredUsers());
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String vcsRootNumberId = makeId("vcsRootNumber");
    presentationManager.applyPresentation(vcsRootNumberId, "VCS roots", myGroupName, null, null);
    publisher.publishStatistic(vcsRootNumberId, myServer.getVcsManager().getAllRegisteredVcsRoots().size());
  }
}
