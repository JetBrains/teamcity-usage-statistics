/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics;

import jetbrains.buildServer.SystemProvided;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for publishing custom usage statistics.
 *
 * @since 6.0
 */
@SystemProvided
public interface UsageStatisticsPublisher {
  /**
   * Call this method to publish your custom usage statistic.
   *
   * @param id Statistic identifier. It is recommended to use your package name as prefix for this id to be sure
   *           it does not clash with other statistics. E.g. "com.myCompanyName.teamcity.statistic.myStatisticName".
   * @param value The value of this statistic.
   *
   * @see UsageStatisticsProvider#accept(UsageStatisticsPublisher)
   */
  void publishStatistic(@NotNull String id, @Nullable Object value);
}
