
package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MuteUsageStatisticsProvider extends BaseFeatureUsageStatisticsProvider {
  @NotNull @NonNls private static final String TESTS = "tests";
  @NotNull private static final Feature[] FEATURES = { new Feature(TESTS, "Test mutes") };

  public MuteUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                     @NotNull ServerPaths serverPaths,
                                     @NotNull ServerResponsibility serverResponsibility) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions());
    eventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void testsMuted(@NotNull final MuteInfo muteInfo) {
        doAddUsage();
      }
    });
  }

  // do not inline this method, see TW-34736
  private void doAddUsage() {
    addUsage(TESTS);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.INVESTIGATION_MUTE;
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "muteUsage";
  }

  @NotNull
  @Override
  protected Feature[] getFeatures() {
    return FEATURES;
  }
}