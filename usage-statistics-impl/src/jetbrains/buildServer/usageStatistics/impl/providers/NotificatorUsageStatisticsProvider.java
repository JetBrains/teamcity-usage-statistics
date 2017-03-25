/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import java.util.List;
import java.util.Map;
import jetbrains.buildServer.notification.NotificationRule;
import jetbrains.buildServer.notification.NotificationRulesManager;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.filters.Filter;
import jetbrains.buildServer.util.filters.FilterUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class NotificatorUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;
  @NotNull private final NotificatorRegistry myNotificatorRegistry;
  @NotNull private final NotificationRulesManager myNotificationRulesManager;

  public NotificatorUsageStatisticsProvider(@NotNull final SBuildServer server,
                                            @NotNull final NotificatorRegistry notificatorRegistry,
                                            @NotNull final NotificationRulesManager notificationRulesManager) {
    myServer = server;
    myNotificatorRegistry = notificatorRegistry;
    myNotificationRulesManager = notificationRulesManager;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.NOTIFIERS;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final Notificator notificator : myNotificatorRegistry.getNotificators()) {
      final String notificatorType = notificator.getNotificatorType();
      final Map<Long, List<NotificationRule>> rules = myNotificationRulesManager.findRulesByNotificatorType(notificatorType);
      FilterUtil.filterMapByKey(rules, new Filter<Long>() {
        public boolean accept(@NotNull final Long userId) {
          return rules.get(userId).size() > 0;
        }
      });
      final int count = rules.size();
      if (count > 0) {
        callback.setUsagesCount(notificatorType, notificator.getDisplayName(), count);
      }
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of all users)";
  }

  @Override
  protected int getTotalUsagesCount(@NotNull final Map<ExtensionType, Integer> extensionUsages) {
    return myServer.getUserModel().getNumberOfRegisteredUsers();
  }
}
