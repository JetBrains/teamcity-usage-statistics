package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.vcs.SVcsRoot;
import jetbrains.buildServer.vcs.VcsRootsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 31.01.14
 */
public abstract class BaseVCSFeatureUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {
  @NotNull private final VcsRootsManager myVcsRootsManager;

  protected BaseVCSFeatureUsageStatisticsProvider(@NotNull final VcsRootsManager vcsRootsManager) {
    myVcsRootsManager = vcsRootsManager;
    setGroupName("VCS Features");
    setIdFormat("jb.vcsFeature[%s]");
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.VCS_FEATURES;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String vcsFeatureId = makeId(getFeatureName());
    presentationManager.applyPresentation(vcsFeatureId, getFeatureDisplayName(), myGroupName, getValueFormatter(), getValueTooltip());
    publisher.publishStatistic(vcsFeatureId, computeValue());
  }

  @NotNull
  protected abstract String getFeatureName();

  @NotNull
  protected abstract String getFeatureDisplayName();

  @Nullable
  protected Object computeValue() {
    int rootCount = 0;
    for (SVcsRoot root : myVcsRootsManager.getAllRegisteredVcsRoots()) {
      if (hasFeature(root)) {
        rootCount++;
      }
    }
    return rootCount;
  }

  protected boolean hasFeature(@NotNull final SVcsRoot root) {
    return false;
  }

  @Nullable
  protected UsageStatisticsFormatter getValueFormatter() {
    return new PercentageFormatter(myVcsRootsManager.getAllRegisteredVcsRoots().size());
  }

  @Nullable
  protected String getValueTooltip() {
    return "VCS root count (% of all VCS roots)";
  }
}
