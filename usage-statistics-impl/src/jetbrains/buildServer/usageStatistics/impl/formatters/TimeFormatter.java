package jetbrains.buildServer.usageStatistics.impl.formatters;

import jetbrains.buildServer.usageStatistics.Formatter;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TimePrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public class TimeFormatter implements Formatter {
  @NotNull
  public String format(@Nullable final Object obj) {
    if (!(obj instanceof Number)) return StringUtil.NA;
    final long seconds = ((Number) obj).longValue() / Dates.ONE_SECOND;
    return TimePrinter.createSecondsFormatter(false).formatTime(seconds);
  }
}
