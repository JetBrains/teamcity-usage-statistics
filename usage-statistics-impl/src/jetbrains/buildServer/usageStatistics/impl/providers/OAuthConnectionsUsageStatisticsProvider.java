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

import java.util.Map;
import java.util.stream.Collectors;
import jetbrains.buildServer.serverSide.ProjectManagerEx;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class OAuthConnectionsUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {

  private final ProjectManagerEx myProjectManager;
  private final OAuthConnectionsManager myConnectionsManager;

  public OAuthConnectionsUsageStatisticsProvider(@NotNull final ProjectManagerEx projectManager,
                                                 @NotNull final OAuthConnectionsManager connectionsManager) {
    myProjectManager = projectManager;
    myConnectionsManager = connectionsManager;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.CONNECTIONS;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    final Map<String, String> connectionTypes = myConnectionsManager.getOAuthProviders().stream()
                                                                    .collect(Collectors.toMap(
                                                                      connectionType -> connectionType.getType(),
                                                                      connectionType -> connectionType.getDisplayName()
                                                                    ));
    for(SProject project: myProjectManager.getActiveProjects()) {
      for (SProjectFeatureDescriptor feature: project.getOwnFeaturesOfType(OAuthConstants.FEATURE_TYPE)) {
        String connectionType = feature.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM);
        if (connectionType != null) {
          String displayName = connectionTypes.get(connectionType);
          if (displayName != null) {
            callback.addUsage(connectionType, displayName);
          }
        }
      }
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "OAuth Connections count (% of all OAuth Connections connections)";
  }
}
