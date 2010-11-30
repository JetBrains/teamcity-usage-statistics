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
import jetbrains.buildServer.serverSide.db.TeamCityDatabaseManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import org.jetbrains.annotations.NotNull;

public class ServerConfigurationUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "Server Configuration";
  private static final long MEGABYTE = 1024 * 1024;

  @NotNull private final TeamCityDatabaseManager myDBManager;

  public ServerConfigurationUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                                    @NotNull final TeamCityDatabaseManager dbManager,
                                                    @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
    myDBManager = dbManager;
    applyPresentations(presentationManager);
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishPlatform(publisher);
    publishDatabaseInfo(publisher);
    publishJavaInfo(publisher);
    publishXmx(publisher);
  }

  private void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation("jb.server.platform", "Platform", ourGroupName, null);
    presentationManager.applyPresentation("jb.server.database", "Database version", ourGroupName, null);
    presentationManager.applyPresentation("jb.server.jdbc", "JDBC driver version", ourGroupName, null);
    presentationManager.applyPresentation("jb.server.java", "Java version", ourGroupName, null);
    presentationManager.applyPresentation("jb.server.javaRuntime", "Java runtime version", ourGroupName, null);
    presentationManager.applyPresentation("jb.server.maxMemory", "Maximum used memory", ourGroupName,
                                          new TypeBasedFormatter<Long>(Long.class) {
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
    publisher.publishStatistic("jb.server.platform", sb.toString());
  }

  private void publishJavaInfo(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jb.server.java", System.getProperty("java.version"));
    publisher.publishStatistic("jb.server.javaRuntime", System.getProperty("java.runtime.version"));
  }

  private void publishDatabaseInfo(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jb.server.database", myDBManager.getDatabaseProductName() + ' ' + myDBManager.getDatabaseMajorVersion() + '.' + myDBManager.getDatabaseMinorVersion());
    publisher.publishStatistic("jb.server.jdbc", myDBManager.getDriverName() + ' ' + myDBManager.getDriverMajorVersion() + '.' + myDBManager.getDriverMinorVersion());
  }

  private void publishXmx(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jb.server.maxMemory", Runtime.getRuntime().maxMemory() / MEGABYTE);
  }
}
