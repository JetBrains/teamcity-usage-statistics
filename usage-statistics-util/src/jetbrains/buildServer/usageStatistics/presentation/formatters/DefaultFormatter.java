
package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultFormatter implements UsageStatisticsFormatter {
  @NotNull
  public String format(@Nullable final Object statisticValue) {
    return StringUtil.stringValueOf(statisticValue, StringUtil.NA);
  }
}