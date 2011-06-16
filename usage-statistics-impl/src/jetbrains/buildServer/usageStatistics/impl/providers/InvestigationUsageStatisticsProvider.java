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

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class InvestigationUsageStatisticsProvider extends BaseFeatureUsageStatisticsProvider {
  @NotNull @NonNls private static final String TESTS = "tests";
  @NotNull @NonNls private static final String BUILD_TYPES = "buildTypes";
  @NotNull private static final Feature[] FEATURES = { new Feature(TESTS, "Test investigations"), new Feature(BUILD_TYPES, "Build configuration investigations") };

  public InvestigationUsageStatisticsProvider(@NotNull final SBuildServer server,
                                              @NotNull final ServerPaths serverPaths,
                                              @NotNull final UsageStatisticsPresentationManager presentationManager,
                                              @NotNull final PluginDescriptor pluginDescriptor) {
    super(server, serverPaths, presentationManager, createDWMPeriodDescriptions(), pluginDescriptor);
    server.addListener(new BuildServerAdapter() {
      @Override
      public void responsibleChanged(@NotNull final SBuildType bt, @NotNull final ResponsibilityInfo oldValue, @NotNull final ResponsibilityInfo newValue, final boolean isUserAction) {
        if (isUserAction && newValue.getState().isActive()) {
          addUsage(BUILD_TYPES);
        }
      }

      @Override
      public void responsibleChanged(@NotNull final SProject project, @Nullable final TestNameResponsibilityEntry oldValue, @NotNull final TestNameResponsibilityEntry newValue, final boolean isUserAction) {
        if (isUserAction && newValue.getState().isActive()) {
          addUsage(TESTS);
        }
      }

      @Override
      public void responsibleChanged(@NotNull final SProject project, @NotNull final Collection<TestName> testNames, @NotNull final ResponsibilityEntry entry, final boolean isUserAction) {
        if (isUserAction && entry.getState().isActive()) {
          for (int i = 0, count = testNames.size(); i < count; i++) {
            addUsage(TESTS);
          }
        }
      }
    });
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "investigationUsage";
  }

  @NotNull
  @Override
  protected Feature[] getFeatures() {
    return FEATURES;
  }
}
