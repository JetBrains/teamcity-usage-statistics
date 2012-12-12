/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import java.util.Map;
import java.util.Set;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.AuthMethod;
import jetbrains.buildServer.serverSide.auth.AuthMethodDescriptor;
import jetbrains.buildServer.serverSide.auth.UserAuthListener;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModelEx;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class AuthMethodUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements UserAuthListener {
  @NotNull private final UserModelEx myUserModel;

  public AuthMethodUsageStatisticsProvider(@NotNull final SBuildServer server,
                                           @NotNull final ServerPaths serverPaths,
                                           @NotNull final UserModelEx userModel,
                                           @NotNull final EventDispatcher<UserAuthListener> userAuthDispatcher) {
    super(server, serverPaths, createDWMPeriodDescriptions());
    myUserModel = userModel;
    userAuthDispatcher.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.AUTH_METHODS;
  }

  public void userLoggedIn(@NotNull final SUser user, @NotNull final AuthMethod<? extends AuthMethodDescriptor> authMethod) {
    if (!myUserModel.isSpecialUser(user)) {
      addUsage(authMethod.getDescriptor().getDisplayName(), user.getId());
    }
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "authMethodsUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "authMethod";
  }

  @NotNull
  @Override
  protected String getToolIdName() {
    return "name";
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String path) {
    return true;
  }

  @Override
  protected int getTotalUsersCount(@NotNull final Map<ICString, Set<ToolUsage>> usages, final long startDate) {
    return myUserModel.getAllUsers().getUsers().size();
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of all users)";
  }
}
