
package jetbrains.buildServer.controllers;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.Controller;

abstract class UsageStatisticsControllerUtil {
  public static void register(@NotNull final Controller controller,
                              @NotNull final AuthorizationInterceptor authInterceptor,
                              @NotNull final WebControllerManager webControllerManager,
                              @NotNull final String path) {
    authInterceptor.addPathBasedPermissionsChecker(path, RequestPermissionsCheckerEx.globalPermissionChecker(Permission.VIEW_USAGE_STATISTICS));

    webControllerManager.registerController(path, controller);
  }
}