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

import jetbrains.buildServer.clouds.server.CloudStatisticsProvider;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
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
    applyPresentations();

    publishNumberOfAgents(publisher);
    publishNumberOfVirtualAgents(publisher);

    publishNumberOfBuildTypes(publisher);
    publishNumberOfActiveBuildTypes(publisher);
    publishNumberOfDependencies(publisher);
    publishNumberOfProjects(publisher);
    publishNumberOfArchivedProjects(publisher);
    publishNumberOfUserGroups(publisher);
    publishNumberOfUsers(publisher);
    publishNumberOfVcsRoots(publisher);

    publishNumberOfCloudImages(publisher);
    publishNumberOfCloudProfiles(publisher);
  }

  protected void applyPresentations() {
    myPresentationManager.applyPresentation(makeId("allRegisteredAgentNumber"), "Registered agents (all)", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("authorizedRegisteredAgentNumber"), "Registered agents (authorized only)", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("agentNumber"), "Agents", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("virtualAgentNumber"), "Virtual Agents", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("buildTypeNumber"), "Build configurations", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("activeBuildTypeNumber"), "Active build configurations", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("snapshotDependencyNumber"), "Snapshot dependencies", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("artifactDependencyNumber"), "Artifact dependencies", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("archivedProjectNumber"), "Archived projects", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("projectNumber"), "Projects", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("userGroupNumber"), "User groups", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("userNumber"), "Users", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("vcsRootNumber"), "VCS roots", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("cloudProfiles"), "Cloud profiles", myGroupName, null);
    myPresentationManager.applyPresentation(makeId("cloudImages"), "Cloud images", myGroupName, null);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    publisher.publishStatistic(makeId("allRegisteredAgentNumber"), buildAgentManager.getRegisteredAgents(true).size());
    publisher.publishStatistic(makeId("authorizedRegisteredAgentNumber"), buildAgentManager.getRegisteredAgents(false).size());
  }

  private void publishNumberOfVirtualAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final int agentNumber = myCloudProvider.getNumberOfRunningInstances();
    publisher.publishStatistic(makeId("virtualAgentNumber"), agentNumber);
  }

  private void publishNumberOfCloudProfiles(@NotNull final UsageStatisticsPublisher publisher) {
    final int agentNumber = myCloudProvider.getNumberOfProfiles();
    publisher.publishStatistic(makeId("cloudProfiles"), agentNumber);
  }

  private void publishNumberOfCloudImages(@NotNull final UsageStatisticsPublisher publisher) {
    final int agentNumber = myCloudProvider.getNumberOfProfiles();
    publisher.publishStatistic(makeId("cloudImages"), agentNumber);
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();
    publisher.publishStatistic(makeId("buildTypeNumber"), buildTypeNumber);
  }

  private void publishNumberOfActiveBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int activeBuildTypeNumber = myServer.getProjectManager().getActiveBuildTypes().size();
    publisher.publishStatistic(makeId("activeBuildTypeNumber"), activeBuildTypeNumber);
  }

  private void publishNumberOfDependencies(@NotNull final UsageStatisticsPublisher publisher) {
    int snapshotDependencies = 0, artifactDependencies = 0;
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      snapshotDependencies += buildType.getDependencies().size();
      artifactDependencies += buildType.getArtifactDependencies().size();
    }
    publisher.publishStatistic(makeId("snapshotDependencyNumber"), snapshotDependencies);
    publisher.publishStatistic(makeId("artifactDependencyNumber"), artifactDependencies);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();
    publisher.publishStatistic(makeId("projectNumber"), projectNumber);
  }

  private void publishNumberOfArchivedProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int archivedProjectNumber = myServer.getProjectManager().getArchivedProjects().size();
    publisher.publishStatistic(makeId("archivedProjectNumber"), archivedProjectNumber);
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher) {
    final int userGroupNumber = myUserGroupManager.getUserGroups().size();
    publisher.publishStatistic(makeId("userGroupNumber"), userGroupNumber);
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher) {
    final int userNumber = myServer.getUserModel().getNumberOfRegisteredUsers();
    publisher.publishStatistic(makeId("userNumber"), userNumber);
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher) {
    final int vcsRootNumber = myServer.getVcsManager().getAllRegisteredVcsRoots().size();
    publisher.publishStatistic(makeId("vcsRootNumber"), vcsRootNumber);
  }
}
