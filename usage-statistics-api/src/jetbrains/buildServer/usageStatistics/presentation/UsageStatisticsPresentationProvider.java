
package jetbrains.buildServer.usageStatistics.presentation;

import jetbrains.buildServer.TeamCityExtension;
import jetbrains.buildServer.UserImplemented;
import org.jetbrains.annotations.NotNull;

/**
 * Extension point for providing custom usage statistics presentation.
 *
 * @since 6.5.2
 * @see jetbrains.buildServer.usageStatistics.UsageStatisticsProvider
 */
@UserImplemented
public interface UsageStatisticsPresentationProvider extends TeamCityExtension {
  /**
   * This method is called every time when TeamCity collects usage statistics values for some reason.
   * Every call of this method is always performed after avery call of {@link jetbrains.buildServer.usageStatistics.UsageStatisticsProvider#accept(jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher)}.
   *
   * @param presentationManager presentation manager
   */
  void accept(@NotNull UsageStatisticsPresentationManager presentationManager);
}