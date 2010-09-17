package jetbrains.buildServer.controllers;

import java.util.*;
import jetbrains.buildServer.usageStatistics.*;
import jetbrains.buildServer.usageStatistics.impl.UsageStatisticsSettingsPersistor;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 24.08.2010
 */
public class UsageStatisticsBean {
  private final boolean myReportingEnabled;
  @NotNull private final Map<String, List<UsageStatisticPresentation>> myStatistics = new TreeMap<String, List<UsageStatisticPresentation>>();

  public UsageStatisticsBean(@NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                             @NotNull final UsageStatisticsCollector statisticsCollector,
                             @NotNull final UsageStatisticsPresentationManagerEx presentationManager) {
    myReportingEnabled = settingsPersistor.loadSettings().isReportingEnabled();

    statisticsCollector.collectStatistics(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        final UsageStatisticPresentation presentation = presentationManager.createPresentation(id, value);
        getOrCreateGroup(presentation.getGroupName()).add(presentation);
      }
    });
  }

  public boolean isReportingEnabled() {
    return myReportingEnabled;
  }

  @NotNull
  public Map<String, List<UsageStatisticPresentation>> getStatistics() {
    return myStatistics;
  }

  @NotNull
  private List<UsageStatisticPresentation> getOrCreateGroup(@NotNull final String groupName) {
    if (!myStatistics.containsKey(groupName)) {
      myStatistics.put(groupName, new ArrayList<UsageStatisticPresentation>());
    }
    return myStatistics.get(groupName);
  }
}
