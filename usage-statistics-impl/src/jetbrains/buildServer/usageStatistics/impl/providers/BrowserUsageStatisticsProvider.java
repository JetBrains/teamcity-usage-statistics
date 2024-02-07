
package jetbrains.buildServer.usageStatistics.impl.providers;

import eu.bitwalker.useragentutils.*;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.usageStatistics.impl.GetRequestDetector;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModelEx;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.TimeService;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.web.util.UserAgentUtil;
import org.jetbrains.annotations.NotNull;

public class BrowserUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements WebUsersProvider, GetRequestDetector.Listener {
  @NotNull private final UserModelEx myUserModel;

  public BrowserUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                        @NotNull ServerPaths serverPaths,
                                        @NotNull ServerResponsibility serverResponsibility,
                                        @NotNull final UserModelEx userModel,
                                        @NotNull final GetRequestDetector getRequestDetector,
                                        @NotNull final TimeService timeService) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions(), timeService);
    myUserModel = userModel;
    getRequestDetector.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.WEB_BROWSERS;
  }

  @NotNull
  public Set<String> getWebUsers(final long fromTimestamp) {
    return getUsers(fromTimestamp);
  }

  public void onGetRequest(@NotNull final HttpServletRequest request, @NotNull final SUser user) {
    if (myUserModel.isSpecialUser(user)) return;

    final UserAgent userAgent = UserAgentUtil.getUserAgent(request);
    if (userAgent == null) return;

    final Browser browser = userAgent.getBrowser();
    final BrowserType browserType = browser.getBrowserType();
    if (!UserAgentUtil.isBrowser(browserType)) return;

    String name = getBrowserGroupIfNeeded(browser, browserType).getName(); // do not report version for non-IE web browsers

    final OperatingSystem operatingSystem = userAgent.getOperatingSystem();
    if (operatingSystem.getDeviceType() != DeviceType.COMPUTER) { // do not specify OS for desktop browsers
      name += " (" + operatingSystem.getGroup().getName() + ")";
    }

    addUsage(name, user.getId());
  }

  @NotNull
  private static Browser getBrowserGroupIfNeeded(@NotNull final Browser browser, @NotNull final BrowserType browserType) {
    final Browser group = browser.getGroup();
    return group == Browser.IE || browserType != BrowserType.WEB_BROWSER ? browser : group;
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "browsersUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "browser";
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

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of web users)";
  }
}