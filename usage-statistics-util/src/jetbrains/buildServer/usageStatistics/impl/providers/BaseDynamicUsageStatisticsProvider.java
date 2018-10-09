/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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