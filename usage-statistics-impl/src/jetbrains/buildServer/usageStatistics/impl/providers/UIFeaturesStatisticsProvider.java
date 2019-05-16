/*
 * Copyright 2000-2019 JetBrains s.r.o.
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

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.users.SimplePropertyKey;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class UIFeaturesStatisticsProvider extends BaseDefaultUsageStatisticsProvider {

  // map of user properties names to display names
  private static final Map<String, String> PROPERTIES = new HashMap<String, String>() {{
    put("useExperimentalOverview", "Use Experimental Overview");
    put("hasSeenExperimentalOverview", "Seen Experimental Overview");
  }};

  @NotNull
  private final UserModel myUserModel;

  public UIFeaturesStatisticsProvider(@NotNull final UserModel userModel) {
    myUserModel = userModel;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager) {

    final Map<SimplePropertyKey, Integer> counters = new HashMap<>();
    PROPERTIES.keySet().forEach(key -> counters.put(new SimplePropertyKey(key), 0));

    myUserModel.getAllUsers().getUsers().forEach(user ->
                                                   counters.forEach((key, value) -> {
                                                     if (user.getBooleanProperty(key)) {
                                                       counters.put(key, counters.get(key) + 1);
                                                     }
                                                   }));
    counters.forEach((simplePropertyKey, count) -> {
      final String id = makeId("feature." + simplePropertyKey.getKey());
      presentationManager.applyPresentation(id, PROPERTIES.get(simplePropertyKey.getKey()), myGroupName, null, null);
      publisher.publishStatistic(id, count);
    });
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.UI_FEATURES;
  }
}
