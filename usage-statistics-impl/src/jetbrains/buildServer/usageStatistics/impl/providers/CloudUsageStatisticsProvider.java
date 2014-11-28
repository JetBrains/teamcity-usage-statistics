/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import jetbrains.buildServer.clouds.*;
import jetbrains.buildServer.clouds.server.impl.CloudManagerBase;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey.Pak
 *         Date: 10/23/2014
 *         Time: 4:16 PM
 */
public class CloudUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {

  @NotNull private final CloudManagerBase myCloudManager;

  public CloudUsageStatisticsProvider(@NotNull final CloudManagerBase manager) {
    myCloudManager = manager;
  }

  @NotNull
  protected String getValueTooltip() {
    return "Cloud statistics";
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    if (myCloudManager.isIntegrationDisabled())
      return;
    final Collection<? extends CloudType> cloudTypes = myCloudManager.getCloudTypes();
    final Map<String,String> cloudTypeCodeNames = new HashMap<String, String>();
    for (CloudType cloudType : cloudTypes) {
      cloudTypeCodeNames.put(cloudType.getCloudCode(), cloudType.getDisplayName());
    }

    publishCloudInfo(publisher, presentationManager, cloudTypeCodeNames);
  }

  private void publishCloudInfo(final UsageStatisticsPublisher publisher,
                                final UsageStatisticsPresentationManager presentationManager,
                                final Map<String, String> cloudTypeCodeNames
  ){
    final Map<String, AtomicInteger> instancesCountByType = new HashMap<String, AtomicInteger>();
    final Map<String, AtomicInteger> imagesByType = new HashMap<String, AtomicInteger>();
    final Map<String, AtomicInteger> profileCountByType = new HashMap<String, AtomicInteger>();

    for (CloudProfile profile : myCloudManager.listProfiles()) {
      final CloudClient cli = myCloudManager.getClient(profile.getProfileId());
      final String cloudName = profile.getCloudName();
      if (instancesCountByType.get(cloudName) == null){
        instancesCountByType.put(cloudName, new AtomicInteger(0));
      }
      if (imagesByType.get(cloudName) == null){
        imagesByType.put(cloudName, new AtomicInteger(0));
      }
      if (profileCountByType.get(cloudName) == null){
        profileCountByType.put(cloudName, new AtomicInteger(0));
      }
      profileCountByType.get(cloudName).incrementAndGet();

      for (CloudImage image : cli.getImages()) {
        imagesByType.get(cloudName).incrementAndGet();
        if (image.getErrorInfo() != null)
          continue;
        for (CloudInstance instance : image.getInstances()) {
          if (instance.getErrorInfo() == null && instance.getStatus().isCanTerminate()) {
            instancesCountByType.get(cloudName).incrementAndGet();
          }
        }
      }
    }

    for (String cloudCodeName : cloudTypeCodeNames.keySet()) {
      if (!profileCountByType.containsKey(cloudCodeName)){
        continue;
      }
      applyData(publisher, presentationManager,
                "profilesCount", "cloud profiles",
                cloudCodeName, cloudTypeCodeNames.get(cloudCodeName),
                profileCountByType.get(cloudCodeName).get());
      applyData(publisher, presentationManager,
                "imagesCount", "cloud images",
                cloudCodeName, cloudTypeCodeNames.get(cloudCodeName),
                imagesByType.get(cloudCodeName).get());
      applyData(publisher, presentationManager,
                "agentsCount", "cloud agents",
                cloudCodeName, cloudTypeCodeNames.get(cloudCodeName),
                instancesCountByType.get(cloudCodeName).get());

    }

  }

  private void applyData(final UsageStatisticsPublisher publisher,
                         final UsageStatisticsPresentationManager presentationManager,
                         final String paramName,
                         final String paramDisplayName,
                         final String cloudCode,
                         final String cloudDisplayName,
                         final int count) {
    final String agentsCountId = makeId(cloudCode, paramName);
    presentationManager.applyPresentation(agentsCountId, String.format("%s %s", cloudDisplayName, paramDisplayName), myGroupName, null, null);
    publisher.publishStatistic(agentsCountId, count);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.CLOUD;
  }
}
