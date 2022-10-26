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

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.https.HttpsConfigurator;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class HttpsConnectorUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {
  private final IntFormatter myFormatter = new IntFormatter();
  private final HttpsConfigurator myHttpsConfigurator;

  public HttpsConnectorUsageStatisticsProvider(@NotNull HttpsConfigurator httpsConfigurator) {
    myHttpsConfigurator = httpsConfigurator;
  }

  @Override
  protected void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull UsageStatisticsPresentationManager presentationManager) {
    final String statId = makeId("enabled");
    publisher.publishStatistic(statId, myHttpsConfigurator.isEnabled() ? 1 : 0);
    presentationManager.applyPresentation(
      statId,
      "HTTPS connector is in use",
      myGroupName,
      myFormatter,
      null
    );
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.HTTPS;
  }

  private class IntFormatter extends TypeBasedFormatter<Integer> {
    public IntFormatter() {
      super(Integer.class);
    }

    @Override
    protected String doFormat(@NotNull Integer statisticValue) {
      return Integer.toString(statisticValue);
    }
  }
}
