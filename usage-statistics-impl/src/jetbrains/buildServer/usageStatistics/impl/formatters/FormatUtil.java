package jetbrains.buildServer.usageStatistics.impl.formatters;

import jetbrains.buildServer.usageStatistics.Formatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public class FormatUtil {
  @NotNull private static final Formatter ourDefaultFormatter = new DefaultFormatter();

  @NotNull
  public static String format(@Nullable final Formatter formatter, @Nullable final Object value) {
    return getNotNullFormatter(formatter).format(value);
  }

  @NotNull
  private static Formatter getNotNullFormatter(@Nullable final Formatter formatter) {
    return formatter == null ? ourDefaultFormatter : formatter;
  }
}
