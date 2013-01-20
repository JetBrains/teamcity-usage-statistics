/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

/**
 * Provides positions of the standard usage statistics groups.
 *
 * @since 6.5.6
 */
public enum UsageStatisticsGroupPosition implements PositionAware {
  GENERAL(PositionConstraint.first()),
  SERVER_CONFIGURATION(after(GENERAL)),
  SERVER_LOAD(after(SERVER_CONFIGURATION)),
  RUNNERS(after(SERVER_LOAD)),
  BUILD_FEATURES(after(SERVER_LOAD, RUNNERS)),
  BUILD_FAILURE_CONDITIONS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES)),
  IDE_PLUGINS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS)),
  WEB_BROWSERS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS)),
  AUTH_METHODS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS)),
  VCS_ROOT_TYPES(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS, AUTH_METHODS)),
  TRIGGERS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS, AUTH_METHODS, VCS_ROOT_TYPES)),
  NOTIFIERS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS, AUTH_METHODS, VCS_ROOT_TYPES, TRIGGERS)),
  ISSUE_TRACKERS(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS, AUTH_METHODS, VCS_ROOT_TYPES, TRIGGERS, NOTIFIERS)),
  INVESTIGATION_MUTE(after(SERVER_LOAD, RUNNERS, BUILD_FEATURES, BUILD_FAILURE_CONDITIONS, IDE_PLUGINS, WEB_BROWSERS, AUTH_METHODS, VCS_ROOT_TYPES, TRIGGERS, NOTIFIERS, ISSUE_TRACKERS)),
  IDE_FEATURES(after(INVESTIGATION_MUTE)),
  COVERAGE_ENGINES(after(IDE_FEATURES)),
  AGENT_JAVA_VERSIONS(after(IDE_FEATURES, COVERAGE_ENGINES)),
  WEB_PAGES_USAGE(after(IDE_FEATURES, COVERAGE_ENGINES, AGENT_JAVA_VERSIONS)),
  DEFAULT(PositionConstraint.last());

  @NotNull private final PositionConstraint myConstraint;

  private UsageStatisticsGroupPosition(@NotNull final PositionConstraint constraint) {
    myConstraint = constraint;
  }

  @NotNull
  public String getOrderId() {
    return name();
  }

  @NotNull
  public PositionConstraint getConstraint() {
    return myConstraint;
  }
  
  @NotNull
  private static PositionConstraint after(@NotNull final UsageStatisticsGroupPosition... groups) {
    final String[] ids = new String[groups.length];
    for (int i = 0; i < groups.length; i++) {
      ids[i] = groups[i].getOrderId();
    }
    return PositionConstraint.after(ids);
  }
}
