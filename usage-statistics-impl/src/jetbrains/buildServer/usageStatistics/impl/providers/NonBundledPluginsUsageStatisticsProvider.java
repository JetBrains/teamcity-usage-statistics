/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.plugins.PluginManager;
import jetbrains.buildServer.plugins.bean.PluginInfo;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.renderers.ListUsageStatisticsGroup;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.plugins.PluginPaths;
import jetbrains.buildServer.web.plugins.bean.ServerPluginInfo;
import jetbrains.buildServer.web.plugins.web.PluginModelBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class NonBundledPluginsUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull @NonNls private static final String NON_BUNDLED_PLUGINS_USAGE_GROUP = "Non-Bundled Plugins Usage";
  @NotNull private final PluginManager myPluginManager;
  @NotNull private final PluginPaths myPaths;

  public NonBundledPluginsUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                  @NotNull final UsageStatisticsPresentationManager presentationManager,
                                                  @NotNull final PluginDescriptor pluginDescriptor,
                                                  @NotNull final PluginManager pluginManager,
                                                  @NotNull final PluginPaths paths) {
    super(server, presentationManager);
    myPluginManager = pluginManager;
    myPaths = paths;
    presentationManager.registerGroupRenderer(NON_BUNDLED_PLUGINS_USAGE_GROUP, new ListUsageStatisticsGroup(pluginDescriptor));
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    final List<PluginModelBean> plugins = new ArrayList<PluginModelBean>();
    for (final PluginInfo pluginInfo : myPluginManager.getDetectedPlugins()) {
      final PluginModelBean pluginModel = new PluginModelBean(myPaths, (ServerPluginInfo)pluginInfo);
      if (!pluginModel.isBundledWithTeamCity()) {
        plugins.add(pluginModel);
      }
    }

    Collections.sort(plugins);

    for (final PluginModelBean plugin : plugins) {
      final String statisticId = String.format("jb.plugin[%s]", plugin.getName());
      myPresentationManager.applyPresentation(statisticId, plugin.getName(), NON_BUNDLED_PLUGINS_USAGE_GROUP, null);
      publisher.publishStatistic(statisticId, 1);
    }
  }
}
