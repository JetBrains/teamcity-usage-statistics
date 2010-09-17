package jetbrains.buildServer.usageStatistics.presentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
public interface UsageStatisticsPresentationManagerEx extends UsageStatisticsPresentationManager {
  @NotNull
  UsageStatisticPresentation createPresentation(@NotNull String id, @Nullable Object value);
}
