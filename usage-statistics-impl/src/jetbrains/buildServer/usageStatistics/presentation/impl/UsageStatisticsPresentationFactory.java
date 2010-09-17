package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
class UsageStatisticsPresentationFactory {
  @NotNull private static final String MISCELLANEOUS = "Miscellaneous";
  @NotNull private static final UsageStatisticsFormatter ourDefaultFormatter = new DefaultFormatter();

  @NotNull private final String myId;
  @Nullable private final String myDisplayName;
  @Nullable private final String myGroupName;
  @Nullable private final UsageStatisticsFormatter myFormatter;

  public UsageStatisticsPresentationFactory(@NotNull final String id,
                                            @Nullable final String displayName,
                                            @Nullable final String groupName,
                                            @Nullable final UsageStatisticsFormatter formatter) {
    myId = id;
    myDisplayName = displayName;
    myGroupName = groupName;
    myFormatter = formatter;
  }

  @NotNull
  public UsageStatisticPresentation createFor(@Nullable final Object value) {
    return new UsageStatisticsPresentationImpl(
      getNotNull(myDisplayName, myId),
      getNotNull(myGroupName, MISCELLANEOUS),
      getNotNull(myFormatter, ourDefaultFormatter).format(value)
    );
  }

  @NotNull
  private static <T> T getNotNull(@Nullable final T value, @NotNull final T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
