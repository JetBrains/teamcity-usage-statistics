
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface WebUsersProvider {
  @NotNull
  Set<String> getWebUsers(long fromTimestamp);
}