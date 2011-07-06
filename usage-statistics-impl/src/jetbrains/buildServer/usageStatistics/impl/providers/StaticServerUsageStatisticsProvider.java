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

import jetbrains.buildServer.clouds.server.CloudStatisticsProvider;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private final BuildServerEx myServer;
  @NotNull private final UserGroupManager myUserGroupManager;
  @NotNull private final CloudStatisticsProvider myCloudProvider;

  public StaticServerUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                             @NotNull final UserGroupManager userGroupManager,
                                             @NotNull final CloudStatisticsProvider cloudProvider) {
    myServer = server;
    myUserGroupManager = userGroupManager;
    myCloudProvider = cloudProvider;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    publishNumberOfAgents(publisher, presentationManager);
    publishNumberOfVirtualAgents(publisher, presentationManager);
    publishNumberOfAgentLicenses(publisher, presentationManager);

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

    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int allRegisteredAgentsNumber = buildAgentManager.getRegisteredAgents(true).size();

    presentationManager.applyPresentation(allRegisteredAgentNumberId, "Registered agents (all)", myGroupName, null, null);
    publisher.publishStatistic(allRegisteredAgentNumberId, allRegisteredAgentsNumber);

    presentationManager.applyPresentation(authorizedRegisteredAgentNumberId, "Registered agents (authorized only)", myGroupName, new PercentageFormatter(allRegisteredAgentsNumber), "Agent count (% of all agents)");
    publisher.publishStatistic(authorizedRegisteredAgentNumberId, buildAgentManager.getRegisteredAgents(false).size());
  }

  private void publishNumberOfVirtualAgents(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String virtualAgentNumberId = makeId("virtualAgentNumber");
    presentationManager.applyPresentation(virtualAgentNumberId, "Virtual agents", myGroupName, null, null);
    publisher.publishStatistic(virtualAgentNumberId, myCloudProvider.getNumberOfRunningInstances());
  }

  private void publishNumberOfAgentLicenses(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String agentLicenseNumberId = makeId("agentLicenseNumber");
    presentationManager.applyPresentation(agentLicenseNumberId, "Agent licenses", myGroupName, null, null);
    publisher.publishStatistic(agentLicenseNumberId, myServer.getLicenseKeysManager().getLicenseList().getLicensedAgentCount());
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

    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();

    presentationManager.applyPresentation(buildTypeNumberId, "Build configurations", myGroupName, null, null);
    publisher.publishStatistic(buildTypeNumberId, buildTypeNumber);

    presentationManager.applyPresentation(activeBuildTypeNumberId, "Active build configurations", myGroupName, new PercentageFormatter(buildTypeNumber), "Build configuration count (% of all build configurations)");
    publisher.publishStatistic(activeBuildTypeNumberId, myServer.getProjectManager().getActiveBuildTypes().size());
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
