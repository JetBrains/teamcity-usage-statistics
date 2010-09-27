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

package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import org.jetbrains.annotations.NotNull;

class UsageStatisticsPresentationImpl implements UsageStatisticPresentation {
  @NotNull private final String myDisplayName;
  @NotNull private final String myGroupName;
  @NotNull private final String myFormattedValue;

  public UsageStatisticsPresentationImpl(@NotNull final String displayName,
                                         @NotNull final String groupName,
                                         @NotNull final String formattedValue) {
    myDisplayName = displayName;
    myGroupName = groupName;
    myFormattedValue = formattedValue;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @NotNull
  public String getGroupName() {
    return myGroupName;
  }

  @NotNull
  public String getFormattedValue() {
    return myFormattedValue;
  }
}
