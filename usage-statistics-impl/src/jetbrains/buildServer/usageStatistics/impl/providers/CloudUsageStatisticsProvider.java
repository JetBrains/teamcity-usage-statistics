
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jetbrains.buildServer.TeamCityCloud;
import jetbrains.buildServer.clouds.CloudClient;
import jetbrains.buildServer.clouds.CloudProfile;
import jetbrains.buildServer.clouds.CloudType;
import jetbrains.buildServer.clouds.server.CloudManagerBase;
import jetbrains.buildServer.clouds.server.CloudStatisticsProvider;
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
  @NotNull private final CloudStatisticsProvider myRuntimeStatisticsCollector;

  public CloudUsageStatisticsProvider(@NotNull final CloudManagerBase manager,
                                      @NotNull final CloudStatisticsProvider runtimeStatisticsProvider) {
    myCloudManager = manager;
    myRuntimeStatisticsCollector = runtimeStatisticsProvider;
  }

  @NotNull
  protected String getValueTooltip() {
    return "Cloud statistics";
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    //TODO handle if necessary
    //if (!myCloudManager.getIntegrationStatus())
    //  return;

    final Collection<? extends CloudType> cloudTypes = myCloudManager.getCloudTypes();
    final Map<String,String> cloudTypeCodeNames = new HashMap<String, String>();
    for (CloudType cloudType : cloudTypes) {
      if (TeamCityCloud.isCloud() && cloudType.getCloudCode().equals("tc")) {
        continue;
      }

      cloudTypeCodeNames.put(cloudType.getCloudCode(), cloudType.getDisplayName());
    }

    publishCloudInfo(publisher, presentationManager, cloudTypeCodeNames);
  }

  private void publishCloudInfo(final UsageStatisticsPublisher publisher,
                                final UsageStatisticsPresentationManager presentationManager,
                                final Map<String, String> cloudTypeCodeNames
  ){
    final Map<String, AtomicInteger> imagesByType = new HashMap<String, AtomicInteger>();
    final Map<String, AtomicInteger> profileCountByType = new HashMap<String, AtomicInteger>();

    for (CloudProfile profile : myCloudManager.listAllProfiles()) {
      final CloudClient cli = myCloudManager.getClient(profile.getProjectId(), profile.getProfileId());
      final String cloudName = profile.getCloudCode();
      if (imagesByType.get(cloudName) == null){
        imagesByType.put(cloudName, new AtomicInteger(0));
      }
      if (profileCountByType.get(cloudName) == null){
        profileCountByType.put(cloudName, new AtomicInteger(0));
      }
      profileCountByType.get(cloudName).incrementAndGet();

      imagesByType.get(cloudName).addAndGet(cli.getImages().size());
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
      final int maxInstances = myRuntimeStatisticsCollector.getMaxValueAndReset(cloudCodeName);
      applyData(publisher, presentationManager,
                "agentsCount", "cloud agents",
                cloudCodeName, cloudTypeCodeNames.get(cloudCodeName),
                maxInstances);

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