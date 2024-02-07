
package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.TimePrinter;
import org.jetbrains.annotations.NotNull;

public class TimeFormatter extends TypeBasedFormatter<Number> {
  public TimeFormatter() {
    super(Number.class);
  }

  @Override
  protected String doFormat(@NotNull final Number time) {
    return TimePrinter.createSecondsFormatter(false).formatTime(time.longValue() / Dates.ONE_SECOND);
  }
}