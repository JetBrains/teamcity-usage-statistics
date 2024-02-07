
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