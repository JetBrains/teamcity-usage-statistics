package jetbrains.buildServer.usageStatistics.presentation.impl;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 17.09.2010
 */
public class UsageStatisticsPresentationManagerImpl implements UsageStatisticsPresentationManagerEx {
  @NotNull private final Map<String, UsageStatisticsPresentationFactory> myPresentationFactories = new HashMap<String, UsageStatisticsPresentationFactory>();

  @NotNull
  public UsageStatisticPresentation createPresentation(@NotNull final String id, @Nullable final Object value) {
    return getFactory(id).createFor(value);
  }

  public void applyPresentation(@NotNull final String id,
                                @Nullable final String displayName,
                                @Nullable final String groupName,
                                @Nullable final UsageStatisticsFormatter formatter) {
    myPresentationFactories.put(id, new UsageStatisticsPresentationFactory(id, displayName, groupName, formatter));
  }

  @NotNull
  private UsageStatisticsPresentationFactory getFactory(@NotNull final String id) {
    if (!myPresentationFactories.containsKey(id)) {
      applyPresentation(id, null, null, null);
    }
    return myPresentationFactories.get(id);
  }
}
