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

package jetbrains.buildServer.usageStatistics.presentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the usage statistics UI representation.
 *
 * @since 6.0
 */
public interface UsageStatisticsPresentationManager {
  /**
   * Registers a UI representation for specified usage statistic.
   *
   * @param id Statistic identifier.
   * @param displayName The string to use in UI for displaying this statistic. If it is null the identifier is used.
   * @param groupName This parameter is used to group the statistics in UI. Statistics with equal group names are put
   *                  in one group. Statistics with the null group name are put in group "Miscellaneous".
   * @param formatter This object determines how the values of this statistic will be shown in UI. If formatter is null, the default
   *                  behavior is used: if the value is not null the {@link java.lang.String#valueOf(Object) String.valueOf(Object)}
   *                  method is used to show the value in UI, otherwise the string "N/A" is used.
   */
  public void applyPresentation(@NotNull String id,
                                @Nullable String displayName,
                                @Nullable String groupName,
                                @Nullable UsageStatisticsFormatter formatter);

  /**
   * Registers a custom renderer for a specific usage statistics group.
   *
   * @param groupName Name of the group.
   * @param extension Extension to register.
   */
  public void registerGroupRenderer(@NotNull String groupName, @NotNull UsageStatisticsGroupExtension extension);
}
