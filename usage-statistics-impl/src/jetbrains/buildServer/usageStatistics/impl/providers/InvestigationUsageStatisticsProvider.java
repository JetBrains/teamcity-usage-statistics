
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class InvestigationUsageStatisticsProvider extends BaseFeatureUsageStatisticsProvider {
  @NotNull @NonNls private static final String TESTS = "tests";
  @NotNull @NonNls private static final String BUILD_TYPES = "buildTypes";
  @NotNull private static final Feature[] FEATURES = { new Feature(TESTS, "Test investigations"), new Feature(BUILD_TYPES, "Build configuration investigations") };

  public InvestigationUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                              @NotNull ServerPaths serverPaths,
                                              @NotNull ServerResponsibility serverResponsibility) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions());
    eventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void responsibleChanged(@NotNull final SBuildType bt, @NotNull final ResponsibilityEntry oldValue, @NotNull final ResponsibilityEntry newValue) {
        addUsageIfNeeded(newValue.getState(), BUILD_TYPES, newValue.getReporterUser() != null);
      }

      @Override
      public void responsibleChanged(@NotNull final SProject project, @NotNull final Collection<TestName> testNames, @NotNull final ResponsibilityEntry entry, final boolean isUserAction) {
        addUsageIfNeeded(entry.getState(), TESTS, isUserAction);
      }
    });
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.INVESTIGATION_MUTE;
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "investigationUsage";
  }

  @NotNull
  @Override
  protected Feature[] getFeatures() {
    return FEATURES;
  }

  private void addUsageIfNeeded(@NotNull final ResponsibilityEntry.State state,
                                @NotNull final String featureName,
                                final boolean isUserAction) {
    if (isUserAction && state.isActive()) {
      addUsage(featureName);
    }
  }
}