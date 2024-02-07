
package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleValueFormatter implements UsageStatisticsFormatter {
  @NotNull private final String myValue;

  public SingleValueFormatter(@NotNull final String value) {
    myValue = value;
  }

  @NotNull
  public String format(@Nullable final Object statisticValue) {
    return myValue;
  }
}