
package jetbrains.buildServer.usageStatistics.util;

import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

public abstract class BaseUsageStatisticsStatePersister extends BasePluginStatePersister {
  public BaseUsageStatisticsStatePersister(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                           @NotNull ServerPaths serverPaths,
                                           @NotNull ServerResponsibility serverResponsibility) {
    super(eventDispatcher, serverPaths, serverResponsibility);
  }

  @NotNull
  @Override
  protected String getPluginName() {
    return "usage-statistics";
  }
}