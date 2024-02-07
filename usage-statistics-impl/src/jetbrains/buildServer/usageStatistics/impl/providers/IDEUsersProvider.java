
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface IDEUsersProvider {
  @NotNull
  Set<String> getIDEUsers(long fromTimestamp);
}