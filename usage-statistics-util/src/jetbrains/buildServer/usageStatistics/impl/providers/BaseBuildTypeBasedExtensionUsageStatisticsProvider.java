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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBuildTypeBasedExtensionUsageStatisticsProvider<T> extends BaseExtensionUsageStatisticsProvider {
  @NotNull protected final SBuildServer myServer;

  public BaseBuildTypeBasedExtensionUsageStatisticsProvider(@NotNull final SBuildServer server) {
    myServer = server;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      final Set<String> collectedTypes = new HashSet<String>();
      for (final T extension : collectExtensions(buildType)) {
        final String type = getExtensionType(extension);
        if (type != null && !collectedTypes.contains(type)) {
          callback.addUsage(type, getExtensionDisplayName(extension, type));
          collectedTypes.add(type);
        }
      }
    }
  }

  @NotNull
  protected abstract Collection<T> collectExtensions(@NotNull SBuildType buildType);

  @Nullable
  protected abstract String getExtensionType(@NotNull T extension);

  @Nullable
  protected abstract String getExtensionDisplayName(@NotNull T extension, @NotNull String extensionType);

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Build configuration count (% of active build configurations)";
  }

  @Override
  protected int getTotalUsagesCount(@NotNull final Map<ExtensionType, Integer> extensionUsages) {
    return myServer.getProjectManager().getActiveBuildTypes().size();
  }
}
