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

package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UsageStatisticsPresentationImpl implements UsageStatisticPresentation {
  @NotNull private final String myId;
  @NotNull private final String myDisplayName;
  @NotNull private final String myFormattedValue;
  @Nullable private final String myValueTooltip;

  public UsageStatisticsPresentationImpl(@NotNull final String id,
                                         @NotNull final String displayName,
                                         @NotNull final String formattedValue,
                                         @Nullable final String valueTooltip) {
    myId = id;
    myDisplayName = displayName;
    myFormattedValue = formattedValue;
    myValueTooltip = valueTooltip;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @NotNull
  public String getFormattedValue() {
    return myFormattedValue;
  }

  @Nullable
  public String getValueTooltip() {
    return myValueTooltip;
  }
}
