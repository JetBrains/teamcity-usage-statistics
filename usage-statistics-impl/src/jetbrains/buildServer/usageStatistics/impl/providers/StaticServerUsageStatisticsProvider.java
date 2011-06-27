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
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;
  @NotNull private final UserGroupManager myUserGroupManager;
  @NotNull private final UsageStatisticsPresentationManager myPresentationManager;
  @NotNull private final CloudStatisticsProvider myCloudProvider;

  public StaticServerUsageStatisticsProvider(@NotNull final SBuildServer server,
                                             @NotNull final UserGroupManager userGroupManager,
                                             @NotNull final UsageStatisticsPresentationManager presentationManager,
                                             @NotNull final CloudStatisticsProvider cloudProvider) {
    myServer = server;
    myUserGroupManager = userGroupManager;
    myPresentationManager = presentationManager;
    myCloudProvider = cloudProvider;
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishNumberOfAgents(publisher);
    publishNumberOfVirtualAgents(publisher);

    publishNumberOfBuildTypes(publisher);
    publishNumberOfDependencies(publisher);
    publishNumberOfProjects(publisher);
    publishNumberOfUserGroups(publisher);
    publishNumberOfUsers(publisher);
    publishNumberOfVcsRoots(publisher);

    publishNumberOfCloudImages(publisher);
    publishNumberOfCloudProfiles(publisher);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final String allRegisteredAgentNumberId = makeId("allRegisteredAgentNumber");
    final String authorizedRegisteredAgentNumberId = makeId("authorizedRegisteredAgentNumber");

    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int allRegisteredAgentsNumber = buildAgentManager.getRegisteredAgents(true).size();

    myPresentationManager.applyPresentation(allRegisteredAgentNumberId, "Registered agents (all)", myGroupName, null);
    publisher.publishStatistic(allRegisteredAgentNumberId, allRegisteredAgentsNumber);

    myPresentationManager.applyPresentation(authorizedRegisteredAgentNumberId, "Registered agents (authorized only)", myGroupName, new PercentageFormatter(allRegisteredAgentsNumber), "Agent count (% of all agents)");
    publisher.publishStatistic(authorizedRegisteredAgentNumberId, buildAgentManager.getRegisteredAgents(false).size());
  }

  private void publishNumberOfVirtualAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final String virtualAgentNumberId = makeId("virtualAgentNumber");
    myPresentationManager.applyPresentation(virtualAgentNumberId, "Virtual Agents", myGroupName, null);
    publisher.publishStatistic(virtualAgentNumberId, myCloudProvider.getNumberOfRunningInstances());
  }

  private void publishNumberOfCloudProfiles(@NotNull final UsageStatisticsPublisher publisher) {
    final String cloudProfilesId = makeId("cloudProfiles");
    myPresentationManager.applyPresentation(cloudProfilesId, "Cloud profiles", myGroupName, null);
    publisher.publishStatistic(cloudProfilesId, myCloudProvider.getNumberOfProfiles());
  }

  private void publishNumberOfCloudImages(@NotNull final UsageStatisticsPublisher publisher) {
    final String cloudImagesId = makeId("cloudImages");
    myPresentationManager.applyPresentation(cloudImagesId, "Cloud images", myGroupName, null);
    publisher.publishStatistic(cloudImagesId, myCloudProvider.getNumberOfProfiles());
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final String buildTypeNumberId = makeId("buildTypeNumber");
    final String activeBuildTypeNumberId = makeId("activeBuildTypeNumber");

    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();

    myPresentationManager.applyPresentation(buildTypeNumberId, "Build configurations", myGroupName, null);
    publisher.publishStatistic(buildTypeNumberId, buildTypeNumber);

    myPresentationManager.applyPresentation(activeBuildTypeNumberId, "Active build configurations", myGroupName, new PercentageFormatter(buildTypeNumber), "Build configuration count (% of all build configurations)");
    publisher.publishStatistic(activeBuildTypeNumberId, myServer.getProjectManager().getActiveBuildTypes().size());
  }

  private void publishNumberOfDependencies(@NotNull final UsageStatisticsPublisher publisher) {
    final String snapshotDependencyNumberId = makeId("snapshotDependencyNumber");
    final String artifactDependencyNumberId = makeId("artifactDependencyNumber");

    int snapshotDependencies = 0, artifactDependencies = 0;
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      snapshotDependencies += buildType.getDependencies().size();
      artifactDependencies += buildType.getArtifactDependencies().size();
    }

    myPresentationManager.applyPresentation(snapshotDependencyNumberId, "Snapshot dependencies", myGroupName, null);
    publisher.publishStatistic(snapshotDependencyNumberId, snapshotDependencies);

    myPresentationManager.applyPresentation(artifactDependencyNumberId, "Artifact dependencies", myGroupName, null);
    publisher.publishStatistic(artifactDependencyNumberId, artifactDependencies);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final String projectNumberId = makeId("projectNumber");
    final String archivedProjectNumberId = makeId("archivedProjectNumber");

    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();

    myPresentationManager.applyPresentation(projectNumberId, "Projects", myGroupName, null);
    publisher.publishStatistic(projectNumberId, projectNumber);

    myPresentationManager.applyPresentation(archivedProjectNumberId, "Archived projects", myGroupName, new PercentageFormatter(projectNumber), "Project count (% of all projects)");
    publisher.publishStatistic(archivedProjectNumberId, myServer.getProjectManager().getArchivedProjects().size());
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher) {
    final String userGroupNumberId = makeId("userGroupNumber");
    myPresentationManager.applyPresentation(userGroupNumberId, "User groups", myGroupName, null);
    publisher.publishStatistic(userGroupNumberId, myUserGroupManager.getUserGroups().size());
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher) {
    final String userNumberId = makeId("userNumber");
    myPresentationManager.applyPresentation(userNumberId, "Users", myGroupName, null);
    publisher.publishStatistic(userNumberId, myServer.getUserModel().getNumberOfRegisteredUsers());
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher) {
    final String vcsRootNumberId = makeId("vcsRootNumber");
    myPresentationManager.applyPresentation(vcsRootNumberId, "VCS roots", myGroupName, null);
    publisher.publishStatistic(vcsRootNumberId, myServer.getVcsManager().getAllRegisteredVcsRoots().size());
  }
}
