/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import java.util.Map;
import java.util.TreeMap;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;

abstract class BaseDynamicUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private final Map<Long, String> myPeriodDescriptions;

  protected BaseDynamicUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                               @NotNull final UsageStatisticsPresentationManager presentationManager,
                                               @NotNull final Map<Long, String> periodDescriptions) {
    super(server, presentationManager);
    myPeriodDescriptions = periodDescriptions;
    applyPresentations(presentationManager);
  }

  protected BaseDynamicUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                               @NotNull final UsageStatisticsPresentationManager presentationManager) {
    this(server, presentationManager, createDefaultPeriodDescriptions());
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final long now = Dates.now().getTime();
    for (final Long period : myPeriodDescriptions.keySet()) {
      accept(publisher, myPeriodDescriptions.get(period), now - period);
    }
  }

  @NotNull
  protected static TreeMap<Long, String> createDefaultPeriodDescriptions() {
    return new TreeMap<Long, String>() {{
      put(Dates.ONE_HOUR, "Hour");
      put(Dates.ONE_DAY, "Day");
      put(Dates.ONE_WEEK, "Week");
    }};
  }

  protected long getThresholdDate() {
    long maxPeriod = 0;
    for (final Long period : myPeriodDescriptions.keySet()) {
      if (period > maxPeriod) {
        maxPeriod = period;
      }
    }
    return Dates.now().getTime() - maxPeriod;
  }

  protected abstract void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull String periodDescription, long startDate);

  protected void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager, @NotNull final String periodDescription) {}

  private void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    for (final String periodDescription : myPeriodDescriptions.values()) {
      applyPresentations(presentationManager, periodDescription);
    }
  }
}