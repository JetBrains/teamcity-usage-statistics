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

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MuteUsageStatisticsProvider extends BaseFeatureUsageStatisticsProvider {
  @NotNull @NonNls private static final String TESTS = "tests";
  @NotNull private static final Feature[] FEATURES = { new Feature(TESTS, "Test mutes") };

  public MuteUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                     @NotNull ServerPaths serverPaths,
                                     @NotNull ServerResponsibility serverResponsibility) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions());
    eventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void testsMuted(@NotNull final MuteInfo muteInfo) {
        doAddUsage();
      }
    });
  }

  // do not inline this method, see TW-34736
  private void doAddUsage() {
    addUsage(TESTS);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.INVESTIGATION_MUTE;
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "muteUsage";
  }

  @NotNull
  @Override
  protected Feature[] getFeatures() {
    return FEATURES;
  }
}
