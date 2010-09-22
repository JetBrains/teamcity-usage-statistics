package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 21.09.2010
 */
public abstract class TypeBasedFormatter<T> implements UsageStatisticsFormatter {
  @NotNull private final Class<T> myType;

  public TypeBasedFormatter(@NotNull final Class<T> type) {
    myType = type;
  }

  @NotNull
  public String format(@Nullable final Object statisticValue) {
    if (statisticValue == null || !myType.isInstance(statisticValue)) return StringUtil.NA;
    //noinspection unchecked
    return doFormat((T)statisticValue);
  }

  protected abstract String doFormat(@NotNull T statisticValue);
}
