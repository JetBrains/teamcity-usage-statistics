
package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UsageStatisticsPresentationFactory {
  @NotNull private static final UsageStatisticsFormatter ourDefaultFormatter = new DefaultFormatter();

  @NotNull private final String myId;
  @Nullable private final String myDisplayName;
  @Nullable private final UsageStatisticsFormatter myFormatter;
  @Nullable private final String myValueTooltip;

  public UsageStatisticsPresentationFactory(@NotNull final String id,
                                            @Nullable final String displayName,
                                            @Nullable final UsageStatisticsFormatter formatter,
                                            @Nullable final String valueTooltip) {
    myId = id;
    myDisplayName = displayName;
    myFormatter = formatter;
    myValueTooltip = valueTooltip;
  }

  @NotNull
  public UsageStatisticPresentation createFor(@Nullable final Object value) {
    return new UsageStatisticsPresentationImpl(
      myId,
      getNotNull(myDisplayName, myId),
      getNotNull(myFormatter, ourDefaultFormatter).format(value),
      myValueTooltip
    );
  }

  @NotNull
  private static <T> T getNotNull(@Nullable final T value, @NotNull final T defaultValue) {
    return value == null ? defaultValue : value;
  }
}