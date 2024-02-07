
package jetbrains.buildServer.usageStatistics.presentation.renderers;

import com.intellij.openapi.util.Key;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface DynamicUsageStatisticsGroupSettings {
  @NotNull @NonNls public static final Key<List<String>> PERIODS = Key.create("dynamic.periods");
  @NotNull @NonNls public static final Key<String> DEFAULT_VALUE = Key.create("dynamic.default.value");
  @NotNull @NonNls public static final Key<Boolean> SORT = Key.create("dynamic.sort");
}