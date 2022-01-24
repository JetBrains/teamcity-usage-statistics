/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.impl.XmlRpcBasedRemoteServer;
import jetbrains.buildServer.serverSide.impl.XmlRpcDispatcher;
import jetbrains.buildServer.serverSide.impl.XmlRpcListener;
import jetbrains.buildServer.serverSide.impl.XmlRpcSession;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.impl.UserModelImpl;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.TimeService;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IDEUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements XmlRpcListener, IDEUsersProvider {

  @NotNull
  private final Cache<String, String> myUserAgentCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(10, TimeUnit.MINUTES).build();

  public IDEUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                    @NotNull ServerPaths serverPaths,
                                    @NotNull ServerResponsibility serverResponsibility,
                                    @NotNull final XmlRpcDispatcher xmlRpcDispatcher,
                                    @NotNull final TimeService timeService) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions(), timeService);
    xmlRpcDispatcher.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.IDE_PLUGINS;
  }

  @NotNull
  public Set<String> getIDEUsers(final long fromTimestamp) {
    return getUsers(fromTimestamp);
  }

  public void remoteMethodCalled(@NotNull final Class targetClass,
                                 @NotNull final String methodName,
                                 @NotNull final Vector params,
                                 @Nullable final XmlRpcSession session) {
    if (targetClass == XmlRpcBasedRemoteServer.class && session != null) {
      final Long userId = session.getAttribute(XmlRpcSession.USER_ID_ATTR, Long.class);
      if (userId != null && !UserModelImpl.isSpecialUserId(userId)) {
        addUsage(prepareUserAgent(session.getUserAgent()), userId);
      }
    }
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "ideUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "ide";
  }

  @NotNull
  @Override
  protected String getToolIdName() {
    return "name";
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String toolId) {
    return true;
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of IDE users)";
  }

  @NotNull
  private String prepareUserAgent(@NotNull final String userAgent) {
    try {
      return myUserAgentCache.get(userAgent, () -> doPrepareUserAgent(userAgent));
    } catch (ExecutionException ignored) {
      return doPrepareUserAgent(userAgent);
    }
  }

  static String doPrepareUserAgent(@NotNull final String userAgent) {
    int endPos = userAgent.indexOf('(');
    String preparedUserAgent = userAgent.replace('/', ' ');
    if (endPos != -1) {
      preparedUserAgent = preparedUserAgent.substring(0, endPos);
    }

    return preparedUserAgent.trim();
  }
}
