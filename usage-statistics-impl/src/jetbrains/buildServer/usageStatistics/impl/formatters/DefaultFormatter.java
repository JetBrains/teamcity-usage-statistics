package jetbrains.buildServer.usageStatistics.impl.formatters;

import jetbrains.buildServer.usageStatistics.Formatter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public class DefaultFormatter implements Formatter {
  @NotNull
  public String format(@Nullable final Object value) {
    return StringUtil.stringValueOf(value, StringUtil.NA);
  }
}
