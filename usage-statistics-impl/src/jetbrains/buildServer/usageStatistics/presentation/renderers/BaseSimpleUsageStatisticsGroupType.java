
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import com.intellij.openapi.util.UserDataHolder;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroup;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class BaseSimpleUsageStatisticsGroupType implements UsageStatisticsGroupType {
  @NotNull private final String myJspPagePath;

  BaseSimpleUsageStatisticsGroupType(@NotNull final PluginDescriptor pluginDescriptor, @NotNull final String jspPageRelativePath) {
    myJspPagePath = pluginDescriptor.getPluginResourcesPath(jspPageRelativePath);
  }

  @NotNull
  public String getJspPagePath() {
    return myJspPagePath;
  }

  @NotNull
  public UsageStatisticsGroup createGroup(@Nullable final UserDataHolder groupSettings) {
    return new SimpleUsageStatisticsGroup();
  }
}