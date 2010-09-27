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

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsPresentationManagerImpl implements UsageStatisticsPresentationManagerEx {
  @NotNull private final Map<String, UsageStatisticsPresentationFactory> myPresentationFactories = new HashMap<String, UsageStatisticsPresentationFactory>();

  @NotNull
  public UsageStatisticPresentation createPresentation(@NotNull final String id, @Nullable final Object value) {
    return getFactory(id).createFor(value);
  }

  public void applyPresentation(@NotNull final String id,
                                @Nullable final String displayName,
                                @Nullable final String groupName,
                                @Nullable final UsageStatisticsFormatter formatter) {
    myPresentationFactories.put(id, new UsageStatisticsPresentationFactory(id, displayName, groupName, formatter));
  }

  @NotNull
  private UsageStatisticsPresentationFactory getFactory(@NotNull final String id) {
    if (!myPresentationFactories.containsKey(id)) {
      applyPresentation(id, null, null, null);
    }
    return myPresentationFactories.get(id);
  }
}
