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

package jetbrains.buildServer.usageStatistics;

import jetbrains.buildServer.TeamCityExtension;
import jetbrains.buildServer.UserImplemented;
import org.jetbrains.annotations.NotNull;

/**
 * Extension point for providing custom usage statistics to show it on the statistics page.
 *
 * @since 6.0
 * @see  jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider
 */
@UserImplemented
public interface UsageStatisticsProvider extends TeamCityExtension {

  /**
   * This method is called every time when TeamCity collects usage statistics values for some reason.
   *
   * @param publisher Object that should be used to publish custom usage statistics.
   */
  void accept(@NotNull UsageStatisticsPublisher publisher);
}
