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

import jetbrains.buildServer.https.HttpsConfigurationUpdateNotificationListener;
import jetbrains.buildServer.https.HttpsConfigurationUpdateNotifier;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class HttpsConnectorUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider
  implements HttpsConfigurationUpdateNotificationListener {
  public static final String CERT_SOURCE_ID_SUBPATH = "certificateSource";
  private volatile int myManualCertUploadsCount = 0;
  private volatile int myAcmeCertUploadsCount = 0;
  private final IntFormatter myFormatter = new IntFormatter();

  public HttpsConnectorUsageStatisticsProvider(@NotNull HttpsConfigurationUpdateNotifier httpsConfigurationUpdateNotifier) {
    httpsConfigurationUpdateNotifier.register(this);
  }

  @Override
  protected void accept(@NotNull UsageStatisticsPublisher publisher, @NotNull UsageStatisticsPresentationManager presentationManager) {
    final String acmeStatId = makeId(CERT_SOURCE_ID_SUBPATH, "acme");
    publisher.publishStatistic(acmeStatId, myAcmeCertUploadsCount);
    presentationManager.applyPresentation(
      acmeStatId,
      "Certificate obtained from ACME server",
      myGroupName,
      myFormatter,
      null
    );

    final String manualStatId = makeId(CERT_SOURCE_ID_SUBPATH, "manual");
    publisher.publishStatistic(manualStatId, myManualCertUploadsCount);
    presentationManager.applyPresentation(
      manualStatId,
      "Manually uploaded certificate",
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

  @Override
  public void certificateUpdated(boolean fetchedFromAcme) {
    if(fetchedFromAcme) {
      myAcmeCertUploadsCount++;
    } else {
      myManualCertUploadsCount++;
    }
  }

  @Override
  public void serverUrlChanged(@NotNull String serverUrl) { }

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
