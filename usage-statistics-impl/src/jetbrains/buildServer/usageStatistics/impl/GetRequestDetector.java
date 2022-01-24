/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.impl;

import java.util.EventListener;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 5/23/12
 */
public class GetRequestDetector extends SimplePageExtension {
  @NotNull private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);

  public GetRequestDetector(@NotNull final PagePlaces pagePlaces, @NotNull final PluginDescriptor pluginDescriptor) {
    super(pagePlaces, PlaceId.ALL_PAGES_FOOTER, "getRequestProcessor", pluginDescriptor.getPluginResourcesPath("empty.jsp"));
    register();
  }

  public void addListener(@NotNull final Listener listener) {
    myDispatcher.addListener(listener);
  }

  @Override
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    if (isGet(request)) {
      SUser user = null;
      try {
        user = SessionUser.getUser(request);
      } catch (final Exception ignore) {} // TW-18334

      if (user != null) {
        myDispatcher.getMulticaster().onGetRequest(request, user);
      }
    }
  }

  public static interface Listener extends EventListener {
    void onGetRequest(@NotNull HttpServletRequest request, @NotNull SUser user);
  }
}
