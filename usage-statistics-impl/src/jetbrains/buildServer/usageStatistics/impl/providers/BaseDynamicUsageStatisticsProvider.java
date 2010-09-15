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

import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;

abstract class BaseDynamicUsageStatisticsProvider implements UsageStatisticsProvider {
  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final long now = Dates.now().getTime();
    accept(publisher, "Hour", now - Dates.ONE_HOUR);
    accept(publisher, "Day", now - Dates.ONE_DAY);
    accept(publisher, "Week", now - Dates.ONE_WEEK);
  }

  protected abstract void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull String periodDescription, long startDate);
}