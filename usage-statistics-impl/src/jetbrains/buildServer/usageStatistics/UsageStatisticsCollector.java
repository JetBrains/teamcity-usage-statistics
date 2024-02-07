
package jetbrains.buildServer.usageStatistics;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

public interface UsageStatisticsCollector {
  boolean isCollectingNow();

  boolean isStatisticsCollected();

  @NotNull
  Date getLastCollectingFinishDate();

  void forceAsynchronousCollectingNow();

  void collectStatisticsAndWait();

  void publishCollectedStatistics(@NotNull UsageStatisticsPublisher publisher);
}