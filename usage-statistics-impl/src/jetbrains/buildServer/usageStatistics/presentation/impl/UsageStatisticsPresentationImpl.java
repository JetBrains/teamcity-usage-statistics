
package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UsageStatisticsPresentationImpl implements UsageStatisticPresentation {
  @NotNull private final String myId;
  @NotNull private final String myDisplayName;
  @NotNull private final String myFormattedValue;
  @Nullable private final String myValueTooltip;

  public UsageStatisticsPresentationImpl(@NotNull final String id,
                                         @NotNull final String displayName,
                                         @NotNull final String formattedValue,
                                         @Nullable final String valueTooltip) {
    myId = id;
    myDisplayName = displayName;
    myFormattedValue = formattedValue;
    myValueTooltip = valueTooltip;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @NotNull
  public String getFormattedValue() {
    return myFormattedValue;
  }

  @Nullable
  public String getValueTooltip() {
    return myValueTooltip;
  }
}