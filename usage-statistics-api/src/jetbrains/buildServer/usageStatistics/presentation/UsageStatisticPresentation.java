/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
 * Represents usage statistic in UI.
 *
 * @since 6.0
 */
public interface UsageStatisticPresentation {
  /**
   * Returns usage statistic id.
   * @return usage statistic id
   */
  @NotNull
  String getId();

  /**
   * Returns display name for the statistic.
   * @return display name for the statistic
   */
  @NotNull
  String getDisplayName();

  /**
   * Returns formatted value for the statistic.
   * @return formatted value for the statistic
   */
  @NotNull
  String getFormattedValue();

  /**
   * Returns the tooltip for the statistic value.
   * @return the tooltip for the statistic value
   * @since 6.5.2
   */
  @Nullable
  String getValueTooltip();
}
