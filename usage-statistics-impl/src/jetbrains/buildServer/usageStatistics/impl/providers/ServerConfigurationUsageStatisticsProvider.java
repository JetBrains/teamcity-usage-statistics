/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.db.TeamCityDatabaseManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import org.jetbrains.annotations.NotNull;

public class ServerConfigurationUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  private static final long MEGABYTE = 1024 * 1024;

  @NotNull private final TeamCityDatabaseManager myDBManager;
  @NotNull private final LoginConfiguration myLoginConfiguration;
  @NotNull private final UsageStatisticsPresentationManager myPresentationManager;

  public ServerConfigurationUsageStatisticsProvider(@NotNull final TeamCityDatabaseManager dbManager,
                                                    @NotNull final LoginConfiguration loginConfiguration,
                                                    @NotNull final UsageStatisticsPresentationManager presentationManager) {
    myDBManager = dbManager;
    myLoginConfiguration = loginConfiguration;
    myPresentationManager = presentationManager;
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishPlatform(publisher);
    publishAuthScheme(publisher);
    publishDatabaseInfo(publisher);
    publishJavaInfo(publisher);
    publishXmx(publisher);
  }

  private void publishPlatform(@NotNull final UsageStatisticsPublisher publisher) {
    final String platformId = makeId("platform");

    final StringBuilder sb = new StringBuilder();
    sb.append(System.getProperty("os.name")).append(" ");
    sb.append(System.getProperty("os.version")).append(" ");
    sb.append(System.getProperty("os.arch"));

    myPresentationManager.applyPresentation(platformId, "Platform", myGroupName, null);
    publisher.publishStatistic(platformId, sb.toString());
  }

  private void publishAuthScheme(@NotNull final UsageStatisticsPublisher publisher) {
    final String authSchemeId = makeId("authScheme");
    myPresentationManager.applyPresentation(authSchemeId, "Authentication scheme", myGroupName, null);
    publisher.publishStatistic(authSchemeId, myLoginConfiguration.getSelectedLoginModuleDescriptor().getDisplayName());
  }

  private void publishJavaInfo(@NotNull final UsageStatisticsPublisher publisher) {
    final String javaId = makeId("java");
    final String javaRuntimeId = makeId("javaRuntime");

    myPresentationManager.applyPresentation(javaId, "Java version", myGroupName, null);
    publisher.publishStatistic(javaId, System.getProperty("java.version"));

    myPresentationManager.applyPresentation(javaRuntimeId, "Java runtime version", myGroupName, null);
    publisher.publishStatistic(javaRuntimeId, System.getProperty("java.runtime.version"));
  }

  private void publishDatabaseInfo(@NotNull final UsageStatisticsPublisher publisher) {
    final String databaseId = makeId("database");
    final String jdbcId = makeId("jdbc");

    myPresentationManager.applyPresentation(databaseId, "Database version", myGroupName, null);
    publisher.publishStatistic(databaseId, myDBManager.getDatabaseProductName() + ' ' + myDBManager.getDatabaseMajorVersion() + '.' + myDBManager.getDatabaseMinorVersion());

    myPresentationManager.applyPresentation(jdbcId, "JDBC driver version", myGroupName, null);
    publisher.publishStatistic(jdbcId, myDBManager.getDriverName() + ' ' + myDBManager.getDriverMajorVersion() + '.' + myDBManager.getDriverMinorVersion());
  }

  private void publishXmx(@NotNull final UsageStatisticsPublisher publisher) {
    final String maxMemoryId = makeId("maxMemory");
    myPresentationManager.applyPresentation(maxMemoryId, "Maximum used memory", myGroupName,
                                            new TypeBasedFormatter<Long>(Long.class) {
                                              @Override
                                              protected String doFormat(@NotNull final Long statisticValue) {
                                                return String.format("%dMb", statisticValue);
                                              }
                                            });
    publisher.publishStatistic(maxMemoryId, Runtime.getRuntime().maxMemory() / MEGABYTE);
  }
}
