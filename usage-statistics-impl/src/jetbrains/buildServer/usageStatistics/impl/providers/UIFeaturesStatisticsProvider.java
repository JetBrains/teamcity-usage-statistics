
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.users.SimplePropertyKey;
import jetbrains.buildServer.users.UserModelEx;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class UIFeaturesStatisticsProvider extends BaseDynamicUsageStatisticsProvider {

  // map of user properties names to display names
  private static final Map<String, String> PROPERTIES = new HashMap<String, String>() {{
    put("useExperimentalOverview", "Use Experimental Overview");
    put("hasSeenExperimentalOverview", "Seen Experimental Overview");
  }};

  @NotNull
  private final UserModelEx myUserModel;

  @NotNull
  private final WebUsersProvider myWebUsersProvider;

  public UIFeaturesStatisticsProvider(@NotNull final UserModelEx userModel,
                                      @NotNull final WebUsersProvider webUsersProvider) {
    super(createDWMPeriodDescriptions(), new PercentageFormatter(1).format(0));
    myUserModel = userModel;
    myWebUsersProvider = webUsersProvider;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    final Map<SimplePropertyKey, Integer> counters = new HashMap<>();
    PROPERTIES.keySet().forEach(key -> counters.put(new SimplePropertyKey(key), 0));
    final List<Long> userIds = CollectionsUtil.convertCollection(myWebUsersProvider.getWebUsers(startDate), Long::valueOf);

    final String valueTooltip = "User count (% of active web users)";
    final UsageStatisticsFormatter formatter = new PercentageFormatter(userIds.size());

    myUserModel.findUsersByIds(userIds).forEach(user ->
                                                  counters.forEach((key, value) -> {
                                                    if (user.getBooleanProperty(key)) {
                                                      counters.put(key, counters.get(key) + 1);
                                                    }
                                                  }));
    counters.forEach((simplePropertyKey, count) -> {
      final String id = makeId("feature", periodDescription, simplePropertyKey.getKey());
      presentationManager.applyPresentation(id, PROPERTIES.get(simplePropertyKey.getKey()), myGroupName, formatter, valueTooltip);
      publisher.publishStatistic(id, count);
    });
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.UI_FEATURES;
  }

  @Override
  protected boolean mustSortStatistics() {
    return false;
  }
}