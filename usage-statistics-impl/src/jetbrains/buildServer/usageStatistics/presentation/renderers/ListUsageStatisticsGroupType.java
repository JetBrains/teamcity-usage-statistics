
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

public class ListUsageStatisticsGroupType extends BaseSimpleUsageStatisticsGroupType {
  public ListUsageStatisticsGroupType(@NotNull final PluginDescriptor pluginDescriptor) {
    super(pluginDescriptor, "renderers/list.jsp");
  }

  @NotNull
  public String getId() {
    return LIST;
  }
}