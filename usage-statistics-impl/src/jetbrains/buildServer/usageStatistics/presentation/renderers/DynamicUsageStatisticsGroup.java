/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.presentation.renderers;

import com.intellij.openapi.util.Pair;
import java.util.*;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicUsageStatisticsGroup implements UsageStatisticsGroup {
  @NotNull private final List<String> myPeriods;
  @NotNull private final String myDefaultValue;
  private final boolean mySort;

  @NotNull private final Object myStatisticsLock = new Object();
  @NotNull private List<DynamicStatistic> myStatistics = new ArrayList<DynamicStatistic>();

  public DynamicUsageStatisticsGroup(@NotNull final List<String> periods,
                                     @NotNull final String defaultValue,
                                     final boolean sort) {
    myPeriods = periods;
    myDefaultValue = defaultValue;
    mySort = sort;
  }

  public void setStatistics(@NotNull final List<UsageStatisticPresentation> statistics) {
    final LinkedHashMap<String, DynamicStatistic> dynamicStatisticsMap = new LinkedHashMap<String, DynamicStatistic>();

    for (final UsageStatisticPresentation statistic : statistics) {
      final int index = extractIndex(statistic);
      if (index != -1) {
        getOrCreate(dynamicStatisticsMap, statistic).setValue(index, statistic.getFormattedValue(), statistic.getValueTooltip());
      }
    }

    final List<DynamicStatistic> dynamicStatistics = new ArrayList<DynamicStatistic>();
    for (final Map.Entry<String, DynamicStatistic> entry : dynamicStatisticsMap.entrySet()) {
      dynamicStatistics.add(entry.getValue());
    }

    if (mySort) {
      Collections.sort(dynamicStatistics, new Comparator<DynamicStatistic>() {
        public int compare(final DynamicStatistic ds1, final DynamicStatistic ds2) {
          return ds1.getDisplayName().compareToIgnoreCase(ds2.getDisplayName());
        }
      });
    }

    synchronized (myStatisticsLock) {
      myStatistics = dynamicStatistics;
    }
  }

  @NotNull
  public List<DynamicStatistic> getStatistics() {
    synchronized (myStatisticsLock) {
      return myStatistics;
    }
  }

  @NotNull
  public List<String> getPeriods() {
    return myPeriods;
  }

  private int extractIndex(@NotNull final UsageStatisticPresentation statistic) {
    String id = statistic.getId();
    final int lbracketIndex = id.indexOf('[');
    if (lbracketIndex != -1) {
      id = id.substring(0, lbracketIndex);
    }

    for (int i = 0, size = myPeriods.size(); i < size; i++) {
      if (id.contains("." + myPeriods.get(i).toLowerCase())) {
        return i;
      }
    }

    return -1;
  }

  @NotNull
  private DynamicStatistic getOrCreate(@NotNull final Map<String, DynamicStatistic> dynamicStatistics,
                                       @NotNull final UsageStatisticPresentation statistic) {
    final String displayName = statistic.getDisplayName();
    if (!dynamicStatistics.containsKey(displayName)) {
      dynamicStatistics.put(displayName, new DynamicStatistic(displayName, myPeriods.size(), myDefaultValue));
    }
    return dynamicStatistics.get(displayName);
  }

  public static class DynamicStatistic {
    private final String myDisplayName;
    private final StatisticValueInfo[] myValueInfos;

    DynamicStatistic(@NotNull final String displayName, final int valuesCount, @NotNull final String defaultValue) {
      myDisplayName = displayName;
      myValueInfos = new StatisticValueInfo[valuesCount];
      Arrays.fill(myValueInfos, new StatisticValueInfo(defaultValue, null));
    }

    @NotNull
    public String getDisplayName() {
      return myDisplayName;
    }

    public void setValue(final int index, @NotNull final String value, @Nullable final String tooltip) {
      myValueInfos[index] = new StatisticValueInfo(value, tooltip);
    }

    @NotNull
    public StatisticValueInfo[] getValueInfos() {
      return myValueInfos;
    }
  }

  public static class StatisticValueInfo extends Pair<String, String> {
    StatisticValueInfo(@NotNull final String value, @Nullable final String tooltip) {
      super(value, tooltip);
    }

    @NotNull
    public String getValue() {
      return getFirst();
    }

    @Nullable
    public String getTooltip() {
      return getSecond();
    }
  }
}
