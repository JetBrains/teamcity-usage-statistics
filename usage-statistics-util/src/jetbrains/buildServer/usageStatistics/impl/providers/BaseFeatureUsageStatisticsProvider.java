/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import java.util.*;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.util.BaseUsageStatisticsStatePersister;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Dates;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseFeatureUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private final Map<String, List<Long>> myFeatureUsages = new HashMap<String, List<Long>>(); // feature name -> timestamps

  protected BaseFeatureUsageStatisticsProvider(@NotNull final SBuildServer server,
                                               @NotNull final ServerPaths serverPaths,
                                               @NotNull final LinkedHashMap<Long, String> periodDescriptions) {
    super(periodDescriptions, null);
    registerPersistor(server, serverPaths);
  }

  @NotNull
  protected abstract String getExternalId();

  @NotNull
  protected abstract Feature[] getFeatures();

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    removeObsoleteUsages();
    for (final Pair<String, String> feature : getFeatures()) {
      final String featureName = feature.getFirst();
      final String statisticId = makeId(periodDescription, featureName);
      presentationManager.applyPresentation(statisticId, feature.getSecond(), myGroupName, null, null);
      publisher.publishStatistic(statisticId, computeFeatureUsagesCount(featureName, startDate));
    }
  }

  @Override
  protected boolean mustSortStatistics() {
    return true;
  }

  protected synchronized void addUsage(@NotNull final String featureName) {
    List<Long> usages = myFeatureUsages.get(featureName);
    if (usages == null) {
      usages = new ArrayList<Long>();
      myFeatureUsages.put(featureName, usages);
    }
    usages.add(Dates.now().getTime());
  }

  private synchronized void removeObsoleteUsages() {
    final Condition<Long> nonObsolete = createDateCondition(getThresholdDate());
    for (final String featureName : new ArrayList<String>(myFeatureUsages.keySet())) {
      final List<Long> usages = myFeatureUsages.get(featureName);
      final List<Long> nonObsoleteUsages = usages.subList(CollectionsUtil.binarySearch(usages, nonObsolete), usages.size());
      if (nonObsoleteUsages.isEmpty()) {
        myFeatureUsages.remove(featureName);
      }
      else {
        myFeatureUsages.put(featureName, nonObsoleteUsages);
      }
    }
  }

  private synchronized int computeFeatureUsagesCount(@NotNull final String featureName, final long startDate) {
    final List<Long> usages = myFeatureUsages.get(featureName);
    if (usages == null) return 0;
    return usages.size() - CollectionsUtil.binarySearch(usages, createDateCondition(startDate));
  }

  @NotNull
  private static Condition<Long> createDateCondition(final long threshold) {
    return new Condition<Long>() {
      public boolean value(final Long date) {
        return date > threshold;
      }
    };
  }

  @NonNls @NotNull private static final String FEATURE = "feature";
  @NonNls @NotNull private static final String NAME = "name";
  @NonNls @NotNull private static final String USAGE = "usage";
  @NonNls @NotNull private static final String TIMESTAMP = "timestamp";

  private synchronized void writeExternal(@NotNull final Element element) {
    removeObsoleteUsages();
    for (final Map.Entry<String, List<Long>> entry : myFeatureUsages.entrySet()) {
      final Element featureElement = new Element(FEATURE);
      featureElement.setAttribute(NAME, entry.getKey());
      element.addContent((Content) featureElement);
      for (final long timestamp : entry.getValue()) {
        final Element usageElement = new Element(USAGE);
        usageElement.setAttribute(TIMESTAMP, String.valueOf(timestamp));
        featureElement.addContent((Content) usageElement);
      }
    }
  }

  private synchronized void readExternal(@NotNull final Element element) {
    myFeatureUsages.clear();
    for (final Object feature : element.getChildren(FEATURE)) {
      if (!(feature instanceof Element)) continue;
      final Element featureElement = (Element) feature;
      final String featureName = featureElement.getAttributeValue(NAME);
      if (featureName == null) continue;
      final List<Long> usages = new ArrayList<Long>();
      myFeatureUsages.put(featureName, usages);
      for (final Object usage : featureElement.getChildren(USAGE)) {
        if (!(usage instanceof Element)) continue;
        final Element usageElement = (Element)usage;
        final String timestampStr = usageElement.getAttributeValue(TIMESTAMP);
        if (timestampStr == null) continue;
        try {
          usages.add(Long.parseLong(timestampStr));
        } catch (final NumberFormatException ignore) {}
      }
      Collections.sort(usages);
    }
    removeObsoleteUsages();
  }

  private void registerPersistor(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    new BaseUsageStatisticsStatePersister(server, serverPaths) {
      @NotNull
      @Override
      protected String getStateName() {
        return BaseFeatureUsageStatisticsProvider.this.getExternalId();
      }

      @Override
      protected void writeExternal(@NotNull final Element element) {
        BaseFeatureUsageStatisticsProvider.this.writeExternal(element);
      }

      @Override
      protected void readExternal(@NotNull final Element element) {
        BaseFeatureUsageStatisticsProvider.this.readExternal(element);
      }
    };
  }

  protected static class Feature extends Pair<String, String> {
    public Feature(@NotNull final String name, @NotNull final String displayName) {
      super(name, displayName);
    }
  }
}
