package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
abstract class BaseUsageStatisticsProvider implements UsageStatisticsProvider {
  protected BaseUsageStatisticsProvider(@NotNull final SBuildServer server) {
    server.registerExtension(UsageStatisticsProvider.class, getClass().getName(), this);
  }
}
