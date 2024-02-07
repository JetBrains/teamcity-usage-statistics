
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import java.util.List;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroup;
import org.jetbrains.annotations.NotNull;

public class SimpleUsageStatisticsGroup implements UsageStatisticsGroup {
  @NotNull private List<UsageStatisticPresentation> myStatistics;

  public void setStatistics(@NotNull final List<UsageStatisticPresentation> statistics) {
    myStatistics = statistics;
  }

  @NotNull
  public List<UsageStatisticPresentation> getStatistics() {
    return myStatistics;
  }
}