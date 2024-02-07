
package jetbrains.buildServer.usageStatistics.presentation;

import com.intellij.openapi.util.Pair;
import java.util.LinkedHashMap;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import org.jetbrains.annotations.NotNull;

public interface UsageStatisticsPresentationManagerEx extends UsageStatisticsPresentationManager {
  /**
   * @param collector
   * @return map: group name -> (jsp page path, group)
   */
  @NotNull
  LinkedHashMap<String, Pair<String, UsageStatisticsGroup>> groupStatistics(@NotNull UsageStatisticsCollector collector);
}