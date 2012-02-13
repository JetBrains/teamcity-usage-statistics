/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.controllers;

import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.Controller;

abstract class UsageStatisticsControllerUtil {
  public static void register(@NotNull final Controller controller,
                              @NotNull final AuthorizationInterceptor authInterceptor,
                              @NotNull final WebControllerManager webControllerManager,
                              @NotNull final String path) {
    authInterceptor.addPathBasedPermissionsChecker(path, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull final AuthorityHolder authorityHolder, @NotNull final HttpServletRequest request) throws AccessDeniedException {
        if (!authorityHolder.isPermissionGrantedGlobally(Permission.VIEW_USAGE_STATISTICS)) {
          throw new AccessDeniedException(authorityHolder, "You do not have enough permissions to view usage statistics");
        }
      }
    });

    webControllerManager.registerController(path, controller);
  }
}
