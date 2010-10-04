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

import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import org.jetbrains.annotations.NotNull;

public class ServerConfigurationUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "Server Configuration";
  private static final long MEGABYTE = 1024 * 1024;

  public ServerConfigurationUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                    @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
    applyPresentations(presentationManager);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishPlatform(publisher);
    publishDatabaseInfo(publisher);
    publishJavaInfo(publisher);
    publishXmx(publisher);
  }

  private void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.serverPlatform", "Platform", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.serverJavaVersion", "Java version", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.serverJavaRuntimeVersion", "Java runtime version", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.serverDatabaseType", "Database type", ourGroupName, null);
    presentationManager.applyPresentation("jetbrains.buildServer.usageStatistics.serverMaxMemory", "Max used memory", ourGroupName, new TypeBasedFormatter<Long>(Long.class) {
      @Override
      protected String doFormat(@NotNull final Long statisticValue) {
        return String.format("%dMb", statisticValue);
      }
    });
  }

  private void publishPlatform(@NotNull final UsageStatisticsPublisher publisher) {
    final StringBuilder sb = new StringBuilder();
    sb.append(System.getProperty("os.name")).append(" ");
    sb.append(System.getProperty("os.version")).append(" ");
    sb.append(System.getProperty("os.arch"));
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.serverPlatform", sb.toString());
  }

  private void publishDatabaseInfo(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.serverDatabaseType", myServer.getSQLRunner().getDatabaseType().humanReadableName);
  }

  private void publishJavaInfo(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.serverJavaVersion", System.getProperty("java.version"));
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.serverJavaRuntimeVersion", System.getProperty("java.runtime.version"));
  }

  private void publishXmx(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.serverMaxMemory", Runtime.getRuntime().maxMemory() / MEGABYTE);
  }
}
