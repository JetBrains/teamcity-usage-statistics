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

import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.BuildAgentManagerEx;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "General (total count)";
  @NotNull private final UserGroupManager myUserGroupManager;

  public StaticServerUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                             @NotNull final UserGroupManager userGroupManager,
                                             @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
    myUserGroupManager = userGroupManager;
    applyPresentations(presentationManager);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishNumberOfAgents(publisher);
    publishNumberOfBuildTypes(publisher);
    publishNumberOfActiveBuildTypes(publisher);
    publishNumberOfDependencies(publisher);
    publishNumberOfProjects(publisher);
    publishNumberOfArchivedProjects(publisher);
    publishNumberOfUserGroups(publisher);
    publishNumberOfUsers(publisher);
    publishNumberOfVcsRoots(publisher);
  }

  protected void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation("jb.agentNumber", "Agents", ourGroupName, null);
    presentationManager.applyPresentation("jb.buildTypeNumber", "Build configurations", ourGroupName, null);
    presentationManager.applyPresentation("jb.activeBuildTypeNumber", "Active build configurations", ourGroupName, null);
    presentationManager.applyPresentation("jb.snapshotDependencyNumber", "Snapshot dependencies", ourGroupName, null);
    presentationManager.applyPresentation("jb.artifactDependencyNumber", "Artifact dependencies", ourGroupName, null);
    presentationManager.applyPresentation("jb.archivedProjectNumber", "Archived projects", ourGroupName, null);
    presentationManager.applyPresentation("jb.projectNumber", "Projects", ourGroupName, null);
    presentationManager.applyPresentation("jb.userGroupNumber", "User groups", ourGroupName, null);
    presentationManager.applyPresentation("jb.userNumber", "Users", ourGroupName, null);
    presentationManager.applyPresentation("jb.vcsRootNumber", "VCS roots", ourGroupName, null);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int agentNumber = buildAgentManager.getRegisteredAgents(true).size()
                          + ((BuildAgentManagerEx)buildAgentManager).getUnregisteredAgents(true).size();
    publisher.publishStatistic("jb.agentNumber", agentNumber);
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();
    publisher.publishStatistic("jb.buildTypeNumber", buildTypeNumber);
  }

  private void publishNumberOfActiveBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int activeBuildTypeNumber = myServer.getProjectManager().getActiveBuildTypes().size();
    publisher.publishStatistic("jb.activeBuildTypeNumber", activeBuildTypeNumber);
  }

  private void publishNumberOfDependencies(@NotNull final UsageStatisticsPublisher publisher) {
    int snapshotDependencies = 0, artifactDependencies = 0;
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      snapshotDependencies += buildType.getDependencies().size();
      artifactDependencies += buildType.getArtifactDependencies().size();
    }
    publisher.publishStatistic("jb.snapshotDependencyNumber", snapshotDependencies);
    publisher.publishStatistic("jb.artifactDependencyNumber", artifactDependencies);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();
    publisher.publishStatistic("jb.projectNumber", projectNumber);
  }

  private void publishNumberOfArchivedProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int archivedProjectNumber = myServer.getProjectManager().getArchivedProjects().size();
    publisher.publishStatistic("jb.archivedProjectNumber", archivedProjectNumber);
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher) {
    final int userGroupNumber = myUserGroupManager.getUserGroups().size();
    publisher.publishStatistic("jb.userGroupNumber", userGroupNumber);
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher) {
    final int userNumber = myServer.getUserModel().getNumberOfRegisteredUsers();
    publisher.publishStatistic("jb.userNumber", userNumber);
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher) {
    final int vcsRootNumber = myServer.getVcsManager().getAllRegisteredVcsRoots().size();
    publisher.publishStatistic("jb.vcsRootNumber", vcsRootNumber);
  }
}
