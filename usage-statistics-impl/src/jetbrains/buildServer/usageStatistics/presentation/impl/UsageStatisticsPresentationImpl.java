package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
class UsageStatisticsPresentationImpl implements UsageStatisticPresentation {
  @NotNull private final String myDisplayName;
  @NotNull private final String myGroupName;
  @NotNull private final String myFormattedValue;

  public UsageStatisticsPresentationImpl(@NotNull final String displayName,
                                         @NotNull final String groupName,
                                         @NotNull final String formattedValue) {
    myDisplayName = displayName;
    myGroupName = groupName;
    myFormattedValue = formattedValue;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @NotNull
  public String getGroupName() {
    return myGroupName;
  }

  @NotNull
  public String getFormattedValue() {
    return myFormattedValue;
  }
}
