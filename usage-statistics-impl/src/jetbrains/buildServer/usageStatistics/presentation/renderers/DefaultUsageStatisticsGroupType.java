
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

public class DefaultUsageStatisticsGroupType extends BaseSimpleUsageStatisticsGroupType {
  public DefaultUsageStatisticsGroupType(@NotNull final PluginDescriptor pluginDescriptor) {
    super(pluginDescriptor, "renderers/default.jsp");
  }

  @NotNull
  public String getId() {
    return DEFAULT;
  }
}