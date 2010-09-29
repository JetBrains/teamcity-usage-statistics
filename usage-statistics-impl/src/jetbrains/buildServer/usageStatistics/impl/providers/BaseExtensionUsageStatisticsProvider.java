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

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Map;
import java.util.TreeMap;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class BaseExtensionUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private final String myGroupName;

  protected BaseExtensionUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                 @NotNull final UsageStatisticsPresentationManager presentationManager,
                                                 @NotNull final String groupName) {
    super(server, presentationManager);
    myGroupName = groupName;
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final Map<ExtensionType, Integer> extensionUsages = doCollectUsages();
    final UsageStatisticsFormatter formatter = new PercentageFormatter(getTotalCount());
    for (final Map.Entry<ExtensionType, Integer> entry : extensionUsages.entrySet()) {
      final ExtensionType type = entry.getKey();
      final String statisticId = "jetbrains.buildServer.usageStatistics." + getId() + "[" + type.getExtensionTypeId() + "]";
      myPresentationManager.applyPresentation(statisticId, prepareDisplayName(type.getExtensionTypeDisplayName()), myGroupName, formatter);
      publisher.publishStatistic(statisticId, entry.getValue());
    }
  }

  @NotNull
  protected abstract String getId();

  protected abstract void collectUsages(@NotNull UsagesCollectorCallback callback);

  @NotNull
  protected abstract String prepareDisplayName(@NotNull String extensionTypeDisplayName);

  protected abstract int getTotalCount();

  @NotNull
  private Map<ExtensionType, Integer> doCollectUsages() {
    final Map<ExtensionType, Integer> extensionUsages = new TreeMap<ExtensionType, Integer>();
    collectUsages(new UsagesCollectorCallback() {
      public void addUsage(@NotNull final String extensionTypeId, @Nullable final String extensionTypeDisplayName) {
        final ExtensionType extensionType = new ExtensionType(extensionTypeId, extensionTypeDisplayName);
        if (extensionUsages.containsKey(extensionType)) {
          extensionUsages.put(extensionType, extensionUsages.get(extensionType) + 1);
        }
        else {
          extensionUsages.put(extensionType, 1);
        }
      }

      public void setUsagesCount(@NotNull final String extensionTypeId, @Nullable final String extensionTypeDisplayName, final int count) {
        extensionUsages.put(new ExtensionType(extensionTypeId, extensionTypeDisplayName), count);
      }
    });
    return extensionUsages;
  }

  protected static interface UsagesCollectorCallback {
    void addUsage(@NotNull String extensionTypeId, @Nullable String extensionTypeDisplayName);

    void setUsagesCount(@NotNull String extensionTypeId, @Nullable String extensionTypeDisplayName, int count);
  }

  private static class ExtensionType implements Comparable<ExtensionType> {
    @NotNull private final String myExtensionTypeId;
    @Nullable private final String myExtensionTypeDisplayName;

    public ExtensionType(@NotNull final String extensionTypeId, @Nullable final String extensionTypeDisplayName) {
      myExtensionTypeId = extensionTypeId;
      myExtensionTypeDisplayName = extensionTypeDisplayName;
    }

    @NotNull
    public String getExtensionTypeId() {
      return myExtensionTypeId;
    }

    @NotNull
    public String getExtensionTypeDisplayName() {
      return myExtensionTypeDisplayName == null ? myExtensionTypeId : myExtensionTypeDisplayName;
    }

    public int compareTo(@NotNull final ExtensionType that) {
      return getExtensionTypeDisplayName().compareToIgnoreCase(that.getExtensionTypeDisplayName());
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof ExtensionType)) return false;

      final ExtensionType that = (ExtensionType)o;

      return myExtensionTypeId.equals(that.myExtensionTypeId);
    }

    @Override
    public int hashCode() {
      return myExtensionTypeId.hashCode();
    }
  }
}
