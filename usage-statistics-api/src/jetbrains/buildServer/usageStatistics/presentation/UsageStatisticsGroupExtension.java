/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the custom renderer for showing usage statistics group in UI.
 *
 * @since 6.0
 */
public interface UsageStatisticsGroupExtension {
  /**
   * Returns the path of the jsp page to include as group body.
   * @return the path of the jsp page to include as group body
   */
  @NotNull
  String getJspPagePath();

  /**
   * Sets the statistics for this group.
   * @param statistics statistics for the specified group
   */
  void setStatistics(@NotNull List<UsageStatisticPresentation> statistics);
}
