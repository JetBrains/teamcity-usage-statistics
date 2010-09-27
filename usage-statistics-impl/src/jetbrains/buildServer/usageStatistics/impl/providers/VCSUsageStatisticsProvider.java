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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.vcs.SVcsRoot;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VCSUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NonNls @NotNull private static final String VCS_USAGE_GROUP = "VCS Usage";

  public VCSUsageStatisticsProvider(@NotNull final SBuildServer server, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final List<SVcsRoot> vcsRoots = myServer.getVcsManager().getAllRegisteredVcsRoots();    
    final UsageStatisticsFormatter formatter = new PercentageFormatter(vcsRoots.size());
    final Map<VCSType, Integer> vcsTypeUsages = collectUsages(vcsRoots);
    for (final Map.Entry<VCSType, Integer> entry : vcsTypeUsages.entrySet()) {
      final VCSType vcsType = entry.getKey();
      final String statisticId = "jetbrains.buildServer.usageStatistics.vcsUsage." + vcsType.getVCSName();
      myPresentationManager.applyPresentation(statisticId, vcsType.getVCSDisplayName() + " root count", VCS_USAGE_GROUP, formatter);
      publisher.publishStatistic(statisticId, entry.getValue());
    }
  }

  @NotNull
  private Map<VCSType, Integer> collectUsages(@NotNull final List<SVcsRoot> vcsRoots) {
    final Map<VCSType, Integer> usages = new TreeMap<VCSType, Integer>();
    for (final SVcsRoot vcsRoot : vcsRoots) {
      final VCSType vcsType = new VCSType(vcsRoot.getVcsName(), vcsRoot.getVcsDisplayName());
      if (usages.containsKey(vcsType)) {
        usages.put(vcsType, usages.get(vcsType) + 1);
      }
      else {
        usages.put(vcsType, 1);
      }
    }    
    return usages;
  }

  private static class VCSType implements Comparable<VCSType> {
    @NotNull private final String myVCSName;
    @Nullable private final String myVCSDisplayName;

    public VCSType(@NotNull final String VCSName, @Nullable final String VCSDisplayName) {
      myVCSName = VCSName;
      myVCSDisplayName = VCSDisplayName;
    }

    @NotNull
    public String getVCSName() {
      return myVCSName;
    }

    @NotNull
    public String getVCSDisplayName() {
      return myVCSDisplayName == null ? myVCSName : myVCSDisplayName;
    }

    public int compareTo(@NotNull final VCSType that) {
      return getVCSDisplayName().compareToIgnoreCase(that.getVCSDisplayName());
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof VCSType)) return false;

      final VCSType that = (VCSType)o;

      return myVCSName.equals(that.myVCSName);
    }

    @Override
    public int hashCode() {
      return myVCSName.hashCode();
    }
  }
}
