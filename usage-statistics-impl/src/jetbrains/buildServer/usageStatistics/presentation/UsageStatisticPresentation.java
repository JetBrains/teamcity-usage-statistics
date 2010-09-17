package jetbrains.buildServer.usageStatistics.presentation;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
public interface UsageStatisticPresentation {
  @NotNull
  String getDisplayName();

  @NotNull
  String getGroupName();

  @NotNull
  String getFormattedValue();
}
