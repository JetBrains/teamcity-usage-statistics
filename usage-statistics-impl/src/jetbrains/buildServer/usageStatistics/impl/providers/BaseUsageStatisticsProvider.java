package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
abstract class BaseUsageStatisticsProvider implements UsageStatisticsProvider {
  @NotNull protected final SBuildServer myServer;
  @NotNull protected final UsageStatisticsPresentationManager myPresentationManager;

  protected BaseUsageStatisticsProvider(@NotNull final SBuildServer server,
                                        @NotNull final UsageStatisticsPresentationManager presentationManager) {
    myServer = server;
    myPresentationManager = presentationManager;
    server.registerExtension(UsageStatisticsProvider.class, getClass().getName(), this);
  }
}
