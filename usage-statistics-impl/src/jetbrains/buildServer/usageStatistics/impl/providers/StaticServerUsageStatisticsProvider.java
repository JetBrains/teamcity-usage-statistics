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
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "Static Server Information";
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
    publishNumberOfProjects(publisher);
    publishNumberOfArchivedProjects(publisher);
    publishNumberOfUserGroups(publisher);
    publishNumberOfUsers(publisher);
    publishNumberOfVcsRoots(publisher);
  }

  protected void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.agentNumber", "Number of agents", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.buildTypeNumber", "Number of build configurations", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.archivedProjectNumber", "Number of archived projects", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.projectNumber", "Number of projects", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.userGroupNumber", "Number of user groups", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.userNumber", "Number of users", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.vcsRootNumber", "Number of VCS roots", ourGroupName, null);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int agentNumber = buildAgentManager.getRegisteredAgents(true).size() + buildAgentManager.getUnregisteredAgents().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.agentNumber", agentNumber);
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.buildTypeNumber", buildTypeNumber);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.projectNumber", projectNumber);
  }

  private void publishNumberOfArchivedProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int archivedProjectNumber = myServer.getProjectManager().getArchivedProjects().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.archivedProjectNumber", archivedProjectNumber);
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher) {
    final int userGroupNumber = myUserGroupManager.getUserGroups().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.userGroupNumber", userGroupNumber);
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher) {
    final int userNumber = myServer.getUserModel().getNumberOfRegisteredUsers();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.userNumber", userNumber);
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher) {
    final int vcsRootNumber = myServer.getVcsManager().getAllRegisteredVcsRoots().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.vcsRootNumber", vcsRootNumber);
  }
}
