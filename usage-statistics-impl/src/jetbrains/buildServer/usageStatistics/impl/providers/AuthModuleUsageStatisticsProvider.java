
package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.auth.AuthenticatedUserInfo;
import jetbrains.buildServer.serverSide.auth.UserAuthListener;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModelEx;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.TimeService;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class AuthModuleUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements UserAuthListener {

  @NotNull
  private final UserModelEx myUserModel;

  public AuthModuleUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                           @NotNull ServerPaths serverPaths,
                                           @NotNull ServerResponsibility serverResponsibility,
                                           @NotNull final UserModelEx userModel,
                                           @NotNull final EventDispatcher<UserAuthListener> userAuthDispatcher,
                                           @NotNull final TimeService timeService) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions(), timeService);
    myUserModel = userModel;
    userAuthDispatcher.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.AUTH_MODULES;
  }

  public void userLoggedIn(@NotNull final AuthenticatedUserInfo authenticatedUserInfo) {
    final SUser user = authenticatedUserInfo.getUser();
    if (!myUserModel.isSpecialUser(user)) {
      addUsage(authenticatedUserInfo.getAuthModule().getType().getDisplayName(), user.getId());
    }
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "authModulesUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "authModule";
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
  protected int getTotalUsersCount(final long startDate) {
    return myUserModel.getAllUsers().getUsers().size();
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of all users)";
  }
}