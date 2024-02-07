
package jetbrains.buildServer.usageStatistics.presentation;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the certain usage statistics group in UI.
 *
 * @since 6.5.2
 */
public interface UsageStatisticsGroup {
  /**
   * Sets the statistics for this group.
   * @param statistics statistics for this group
   */
  void setStatistics(@NotNull List<UsageStatisticPresentation> statistics);
}