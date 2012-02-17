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

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.LicensingPolicy;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.db.TeamCityDatabaseManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.version.ServerVersionHolder;
import org.jetbrains.annotations.NotNull;

public class ServerConfigurationUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {
  private static final long MEGABYTE = 1024 * 1024;

  @NotNull private final TeamCityDatabaseManager myDBManager;
  @NotNull private final LoginConfiguration myLoginConfiguration;
  @NotNull private final LicensingPolicy myLicensingPolicy;

  public ServerConfigurationUsageStatisticsProvider(@NotNull final TeamCityDatabaseManager dbManager,
                                                    @NotNull final LoginConfiguration loginConfiguration,
                                                    @NotNull final SBuildServer buildServer) {
    myDBManager = dbManager;
    myLoginConfiguration = loginConfiguration;
    myLicensingPolicy = buildServer.getLicensingPolicy();
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.SERVER_CONFIGURATION;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    publishPlatform(publisher, presentationManager);
    publishAuthScheme(publisher, presentationManager);
    publishDatabaseInfo(publisher, presentationManager);
    publishJavaInfo(publisher, presentationManager);
    publishXmx(publisher, presentationManager);
    publishLicenseType(publisher, presentationManager);
    publishAgentLicenses(publisher, presentationManager);
    publishTCVersion(publisher, presentationManager);
  }

  private void publishPlatform(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String platformId = makeId("platform");

    final StringBuilder sb = new StringBuilder();
    sb.append(System.getProperty("os.name")).append(" ");
    sb.append(System.getProperty("os.version")).append(" ");
    sb.append(System.getProperty("os.arch"));

    presentationManager.applyPresentation(platformId, "Platform", myGroupName, null, null);
    publisher.publishStatistic(platformId, sb.toString());
  }

  private void publishAuthScheme(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String authSchemeId = makeId("authScheme");
    presentationManager.applyPresentation(authSchemeId, "Authentication scheme", myGroupName, null, null);
    publisher.publishStatistic(authSchemeId, myLoginConfiguration.getSelectedLoginModuleDescriptor().getDisplayName());
  }

  private void publishJavaInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String javaId = makeId("java");
    final String javaRuntimeId = makeId("javaRuntime");

    presentationManager.applyPresentation(javaId, "Java version", myGroupName, null, null);
    publisher.publishStatistic(javaId, System.getProperty("java.version"));

    presentationManager.applyPresentation(javaRuntimeId, "Java runtime version", myGroupName, null, null);
    publisher.publishStatistic(javaRuntimeId, System.getProperty("java.runtime.version"));
  }

  private void publishDatabaseInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String databaseId = makeId("database");
    final String jdbcId = makeId("jdbc");

    presentationManager.applyPresentation(databaseId, "Database version", myGroupName, null, null);
    publisher.publishStatistic(databaseId, myDBManager.getDatabaseProductName() + ' ' + myDBManager.getDatabaseMajorVersion() + '.' + myDBManager.getDatabaseMinorVersion());

    presentationManager.applyPresentation(jdbcId, "JDBC driver version", myGroupName, null, null);
    publisher.publishStatistic(jdbcId, myDBManager.getDriverName() + ' ' + myDBManager.getDriverMajorVersion() + '.' + myDBManager.getDriverMinorVersion());
  }

  private void publishXmx(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String maxMemoryId = makeId("maxMemory");
    presentationManager.applyPresentation(maxMemoryId, "Maximum used memory", myGroupName,
                                            new TypeBasedFormatter<Long>(Long.class) {
                                              @Override
                                              protected String doFormat(@NotNull final Long statisticValue) {
                                                return String.format("%dMb", statisticValue);
                                              }
                                            }, null);
    publisher.publishStatistic(maxMemoryId, Runtime.getRuntime().maxMemory() / MEGABYTE);
  }

  private void publishLicenseType(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String licenseTypeId = makeId("licenseType");
    presentationManager.applyPresentation(licenseTypeId, "License type", myGroupName, null, null);
    publisher.publishStatistic(licenseTypeId, myLicensingPolicy.isEnterpriseMode() ? "Enterprise" : "Professional");
  }

  private void publishAgentLicenses(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String agentLicensesId = makeId("agentLicenses");
    presentationManager.applyPresentation(agentLicensesId, "Agent licenses", myGroupName, new TypeBasedFormatter<Integer>(Integer.class) {
      @Override
      protected String doFormat(@NotNull final Integer count) {
        return count < 0 ? "unlimited" : String.valueOf(count);
      }
    }, null);
    publisher.publishStatistic(agentLicensesId, myLicensingPolicy.getMaxNumberOfAuthorizedAgents());
  }

  private void publishTCVersion(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String versionId = makeId("version");
    presentationManager.applyPresentation(versionId, "Server version", myGroupName, null, null);
    publisher.publishStatistic(versionId, ServerVersionHolder.getVersion().getDisplayVersion());

    final String buildId = makeId("build");
    presentationManager.applyPresentation(buildId, "Server build", myGroupName, null, null);
    publisher.publishStatistic(buildId, ServerVersionHolder.getVersion().getBuildNumber());
  }
}
