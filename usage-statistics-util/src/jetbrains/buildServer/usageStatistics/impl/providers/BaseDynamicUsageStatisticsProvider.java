
package jetbrains.buildServer.usageStatistics.impl.providers;

import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupType;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.renderers.DynamicUsageStatisticsGroupSettings;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseDynamicUsageStatisticsProvider extends BaseUsageStatisticsProvider {

  @NotNull
  private final LinkedHashMap<Long, String> myPeriodDescriptions;

  @Nullable
  private final String myDefaultValue;

  @SuppressWarnings("WeakerAccess")
  public BaseDynamicUsageStatisticsProvider(@NotNull final LinkedHashMap<Long, String> periodDescriptions,
                                            @Nullable final String defaultValue) {
    myPeriodDescriptions = periodDescriptions;
    myDefaultValue = defaultValue;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final long now = Dates.now().getTime();
    for (final Long period : myPeriodDescriptions.keySet()) {
      accept(publisher, presentationManager, myPeriodDescriptions.get(period).toLowerCase(), now - period);
    }
  }

  @SuppressWarnings("WeakerAccess")
  @NotNull
  protected static LinkedHashMap<Long, String> createDWMPeriodDescriptions() {
    return new LinkedHashMap<Long, String>() {{
      put(Dates.ONE_DAY, "Day");
      put(Dates.ONE_WEEK, "Week");
      put(30 * Dates.ONE_DAY, "Month");
    }};
  }

  long getThresholdDate() {
    long maxPeriod = 0;
    for (final Long period: myPeriodDescriptions.keySet()) {
      if (period > maxPeriod) {
        maxPeriod = period;
      }
    }
    return Dates.now().getTime() - maxPeriod;
  }

  protected abstract void accept(@NotNull UsageStatisticsPublisher publisher,
                                 @NotNull UsageStatisticsPresentationManager presentationManager,
                                 @NotNull String periodDescription,
                                 long startDate);

  protected abstract boolean mustSortStatistics();

  @Override
  protected void setupGroup(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    final List<String> periods = new ArrayList<String>(myPeriodDescriptions.size());
    for (final Map.Entry<Long, String> entry : myPeriodDescriptions.entrySet()) {
      periods.add(entry.getValue());
    }

    final UserDataHolder groupSettings = new UserDataHolderBase();
    groupSettings.putUserData(DynamicUsageStatisticsGroupSettings.PERIODS, periods);
    groupSettings.putUserData(DynamicUsageStatisticsGroupSettings.DEFAULT_VALUE, myDefaultValue);
    groupSettings.putUserData(DynamicUsageStatisticsGroupSettings.SORT, mustSortStatistics());

    presentationManager.setGroupType(myGroupName, UsageStatisticsGroupType.DYNAMIC, getGroupPosition(), groupSettings);
  }
}