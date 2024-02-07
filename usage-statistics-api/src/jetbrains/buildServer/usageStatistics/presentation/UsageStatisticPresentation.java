
package jetbrains.buildServer.usageStatistics.presentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents usage statistic in UI.
 *
 * @since 6.0
 */
public interface UsageStatisticPresentation {
  /**
   * Returns usage statistic id.
   * @return usage statistic id
   */
  @NotNull
  String getId();

  /**
   * Returns display name for the statistic.
   * @return display name for the statistic
   */
  @NotNull
  String getDisplayName();

  /**
   * Returns formatted value for the statistic.
   * @return formatted value for the statistic
   */
  @NotNull
  String getFormattedValue();

  /**
   * Returns the tooltip for the statistic value.
   * @return the tooltip for the statistic value
   * @since 6.5.2
   */
  @Nullable
  String getValueTooltip();
}