/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UsageStatisticsPresentationFactory {
  @NotNull private static final UsageStatisticsFormatter ourDefaultFormatter = new DefaultFormatter();

  @NotNull private final String myId;
  @Nullable private final String myDisplayName;
  @Nullable private final UsageStatisticsFormatter myFormatter;
  @Nullable private final String myValueTooltip;

  public UsageStatisticsPresentationFactory(@NotNull final String id,
                                            @Nullable final String displayName,
                                            @Nullable final UsageStatisticsFormatter formatter,
                                            @Nullable final String valueTooltip) {
    myId = id;
    myDisplayName = displayName;
    myFormatter = formatter;
    myValueTooltip = valueTooltip;
  }

  @NotNull
  public UsageStatisticPresentation createFor(@Nullable final Object value) {
    return new UsageStatisticsPresentationImpl(
      myId,
      getNotNull(myDisplayName, myId),
      getNotNull(myFormatter, ourDefaultFormatter).format(value),
      myValueTooltip
    );
  }

  @NotNull
  private static <T> T getNotNull(@Nullable final T value, @NotNull final T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
