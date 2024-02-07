
package jetbrains.buildServer.usageStatistics.presentation;

import com.intellij.openapi.util.UserDataHolder;
import jetbrains.buildServer.TeamCityExtension;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the custom renderer for showing usage statistics group in UI.
 *
 * @since 6.5.2
 */
public interface UsageStatisticsGroupType extends TeamCityExtension {
  @NotNull @NonNls public static final String DEFAULT = "default";
  @NotNull @NonNls public static final String DYNAMIC = "dynamic";
  @NotNull @NonNls public static final String LIST = "list";

  /**
   * Returns group type id.
   * @return group type id.
   */
  @NotNull
  String getId();

  /**
   * Returns the path of the jsp page to include as group body.
   * @return the path of the jsp page to include as group body
   */
  @NotNull
  String getJspPagePath();

  /**
   * Creates usage statistics group of this type
   * @param groupSettings group settings (specific to this type)
   * @return new usage statistics group of this type
   */
  @NotNull
  UsageStatisticsGroup createGroup(@Nullable UserDataHolder groupSettings);
}