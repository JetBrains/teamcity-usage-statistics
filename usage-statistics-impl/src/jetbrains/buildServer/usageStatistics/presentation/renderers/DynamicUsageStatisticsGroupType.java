
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import com.intellij.openapi.util.UserDataHolder;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroup;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupType;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicUsageStatisticsGroupType implements UsageStatisticsGroupType {
  @NotNull private final String myJspPagePath;

  public DynamicUsageStatisticsGroupType(@NotNull final PluginDescriptor pluginDescriptor) {
    myJspPagePath = pluginDescriptor.getPluginResourcesPath("renderers/dynamic.jsp");
  }

  @NotNull
  public String getId() {
    return DYNAMIC;
  }

  @NotNull
  public String getJspPagePath() {
    return myJspPagePath;
  }

  @NotNull
  public UsageStatisticsGroup createGroup(@Nullable final UserDataHolder groupSettings) {
    List<String> periods = null;
    String defaultValue = null;
    Boolean sort = null;

    if (groupSettings != null) {
      periods = groupSettings.getUserData(DynamicUsageStatisticsGroupSettings.PERIODS);
      defaultValue = groupSettings.getUserData(DynamicUsageStatisticsGroupSettings.DEFAULT_VALUE);
      sort = groupSettings.getUserData(DynamicUsageStatisticsGroupSettings.SORT);
    }

    return new DynamicUsageStatisticsGroup(
      periods == null ? Collections.<String>emptyList() : periods,
      defaultValue == null ? new DefaultFormatter().format(null) : defaultValue,
      sort != null && sort
    );
  }
}