
package jetbrains.buildServer.usageStatistics.presentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class is used to customize the string representation of the statistic value in UI
 *
 * @since 6.0
 */
public interface UsageStatisticsFormatter {
  @NotNull
  String format(@Nullable Object statisticValue);
}