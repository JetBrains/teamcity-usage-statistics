
package jetbrains.buildServer.usageStatistics;

import jetbrains.buildServer.TeamCityExtension;
import jetbrains.buildServer.UserImplemented;
import org.jetbrains.annotations.NotNull;

/**
 * Extension point for providing custom usage statistics to show it on the statistics page.
 *
 * @since 6.0
 * @see  jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider
 */
@UserImplemented
public interface UsageStatisticsProvider extends TeamCityExtension {

  /**
   * This method is called every time when TeamCity collects usage statistics values for some reason.
   *
   * @param publisher Object that should be used to publish custom usage statistics.
   */
  void accept(@NotNull UsageStatisticsPublisher publisher);
}