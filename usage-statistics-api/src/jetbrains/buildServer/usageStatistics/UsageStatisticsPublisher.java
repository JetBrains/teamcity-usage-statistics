
package jetbrains.buildServer.usageStatistics;

import jetbrains.buildServer.SystemProvided;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for publishing custom usage statistics.
 *
 * @since 6.0
 */
@SystemProvided
public interface UsageStatisticsPublisher {
  /**
   * Call this method to publish your custom usage statistic.
   *
   * @param id Statistic identifier. It is recommended to use your package name as prefix for this id to be sure
   *           it does not clash with other statistics. E.g. "com.myCompanyName.teamcity.statistic.myStatisticName".
   * @param value The value of this statistic.
   *
   * @see UsageStatisticsProvider#accept(UsageStatisticsPublisher)
   */
  void publishStatistic(@NotNull String id, @Nullable Object value);
}