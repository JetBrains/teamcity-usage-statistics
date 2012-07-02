package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.*;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBuildTypeBasedExtensionUsageStatisticsProvider<T> extends BaseExtensionUsageStatisticsProvider {
  @NotNull protected final SBuildServer myServer;

  public BaseBuildTypeBasedExtensionUsageStatisticsProvider(@NotNull final SBuildServer server) {
    myServer = server;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      final Set<String> collectedTypes = new HashSet<String>();
      for (final T extension : collectExtensions(buildType)) {
        final String type = getExtensionType(extension);
        if (type != null && !collectedTypes.contains(type)) {
          callback.addUsage(type, getExtensionDisplayName(extension, type));
          collectedTypes.add(type);
        }
      }
    }
  }

  @NotNull
  protected abstract Collection<T> collectExtensions(@NotNull SBuildType buildType);

  @Nullable
  protected abstract String getExtensionType(@NotNull T extension);

  @Nullable
  protected abstract String getExtensionDisplayName(@NotNull T extension, @NotNull String extensionType);

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Build configuration count (% of active build configurations)";
  }

  @Override
  protected int getTotalUsagesCount(@NotNull final Map<ExtensionType, Integer> extensionUsages) {
    return myServer.getProjectManager().getActiveBuildTypes().size();
  }
}
