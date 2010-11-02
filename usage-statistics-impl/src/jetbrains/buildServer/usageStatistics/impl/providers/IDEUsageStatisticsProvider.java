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

import java.util.Vector;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.impl.XmlRpcBasedRemoteServer;
import jetbrains.buildServer.serverSide.impl.XmlRpcDispatcher;
import jetbrains.buildServer.serverSide.impl.XmlRpcListener;
import jetbrains.buildServer.serverSide.impl.XmlRpcSession;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IDEUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements XmlRpcListener {
  @NotNull @NonNls private static final String IDE_USAGE_GROUP = "IDE Usage";

  public IDEUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                    @NotNull final ServerPaths serverPaths,
                                    @NotNull final XmlRpcDispatcher xmlRpcDispatcher,
                                    @NotNull final UsageStatisticsPresentationManager presentationManager,
                                    @NotNull final PluginDescriptor pluginDescriptor) {
    super(server, serverPaths, presentationManager, pluginDescriptor, IDE_USAGE_GROUP);
    xmlRpcDispatcher.addListener(this);
  }

  public void remoteMethodCalled(@NotNull final Class targetClass,
                                 @NotNull final String methodName,
                                 @NotNull final Vector params,
                                 @Nullable final XmlRpcSession session) {
    if (targetClass == XmlRpcBasedRemoteServer.class && session != null) {
      final Long userId = session.getUserId();
      if (userId != null) {
        addUsage(prepareUserAgent(session.getUserAgent()), userId);
      }
    }
  }

  @NotNull
  @Override
  protected String getId() {
    return "ide";
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

  @NotNull
  @Override
  protected String prepareDisplayName(@NotNull final String ideName) {
    return ideName + " user count";
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String toolId) {
    return true;
  }

  @NotNull
  private static String prepareUserAgent(@NotNull final String userAgent) {
    int endPos = userAgent.indexOf('/');
    if (endPos == -1) {
      endPos = userAgent.indexOf('(');
    }
    else {
      endPos = userAgent.indexOf('.', endPos + 1);
    }

    String preparedUserAgent = userAgent.replace('/', ' ');
    if (endPos != -1) {
      preparedUserAgent = preparedUserAgent.substring(0, endPos);
    }

    return preparedUserAgent.trim();
  }
}
