
package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDefaultUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @Override
  protected void setupGroup(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.setGroupType(myGroupName, UsageStatisticsGroupType.DEFAULT, getGroupPosition(), null);
  }
}