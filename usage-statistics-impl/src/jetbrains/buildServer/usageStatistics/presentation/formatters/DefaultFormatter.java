package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public class DefaultFormatter implements UsageStatisticsFormatter {
  @NotNull
  public String format(@Nullable final Object statisticValue) {
    return StringUtil.stringValueOf(statisticValue, StringUtil.NA);
  }
}
