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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import java.util.List;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationScheme;
import jetbrains.buildServer.maintenance.StartupContext;
import jetbrains.buildServer.serverSide.LicenseMode;
import jetbrains.buildServer.serverSide.LicensingPolicy;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.db.TeamCityDatabaseManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.SingleValueFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TimeFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TrimFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.version.ServerVersionHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerConfigurationUsageStatisticsProvider extends BaseDefaultUsageStatisticsProvider {
  private static final long MEGABYTE = 1024 * 1024;

  @NotNull private final TeamCityDatabaseManager myDBManager;
  @NotNull private final LoginConfiguration myLoginConfiguration;
  @NotNull private final LicensingPolicy myLicensingPolicy;
  @NotNull private final ServerSettings myServerSettings;
  @NotNull private final StartupContext myStartupContext;

  public ServerConfigurationUsageStatisticsProvider(@NotNull final TeamCityDatabaseManager dbManager,
                                                    @NotNull final LoginConfiguration loginConfiguration,
                                                    @NotNull final SBuildServer buildServer,
                                                    @NotNull final ServerSettings serverSettings,
                                                    @NotNull final StartupContext startupContext
                                                    ) {
    myDBManager = dbManager;
    myLoginConfiguration = loginConfiguration;
    myLicensingPolicy = buildServer.getLicensingPolicy();
    myServerSettings = serverSettings;
    myStartupContext = startupContext;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.SERVER_CONFIGURATION;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    publishServerId(publisher, presentationManager);
    publishPlatform(publisher, presentationManager);
    publishAuthModules(publisher, presentationManager);
    publishDatabaseInfo(publisher, presentationManager);
    publishJavaInfo(publisher, presentationManager);
    publishXmx(publisher, presentationManager);
    publishLicenseTypeAndMode(publisher, presentationManager);
    publishAgentLicenses(publisher, presentationManager);
    publishTCVersion(publisher, presentationManager);
    publishServerStartData(publisher, presentationManager);
    publishServerDistributionType(publisher, presentationManager);
  }

  private void publishServerId(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String serverIdId = makeId("id");
    presentationManager.applyPresentation(serverIdId, "Server ID", myGroupName, null, null);
    publisher.publishStatistic(serverIdId, myServerSettings.getServerUUID());
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

  private void publishAuthModules(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String loginModules = makeId("auth.loginModules");
    final String httpAuthSchemes = makeId("auth.httpAuthSchemes");

    final String loginModulesValue = join(myLoginConfiguration.getConfiguredLoginModules());
    final String httpAuthSchemesValue = join(myLoginConfiguration.getConfiguredAuthModules(HttpAuthenticationScheme.class));

    final TrimFormatter formatter = new TrimFormatter(50);

    presentationManager.applyPresentation(loginModules, "Login modules", myGroupName, formatter, getTooltip(formatter, loginModulesValue));
    presentationManager.applyPresentation(httpAuthSchemes, "HTTP authentication schemes", myGroupName, formatter, getTooltip(formatter, httpAuthSchemesValue));

    publisher.publishStatistic(loginModules, loginModulesValue);
    publisher.publishStatistic(httpAuthSchemes, httpAuthSchemesValue);
  }

  @NotNull
  private static <T extends AuthModuleType> String join(@NotNull final List<AuthModule<T>> authModules) {
    return StringUtil.join(authModules, new Function<AuthModule<T>, String>() {
      @NotNull
      public String fun(@NotNull final AuthModule<T> authModule) {
        return authModule.getType().getDisplayName();
      }
    }, ", ");
  }

  @Nullable
  private static String getTooltip(@NotNull final UsageStatisticsFormatter formatter, @NotNull final String value) {
    return value.equals(formatter.format(value)) ? null : value;
  }

  private void publishJavaInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String javaId = makeId("java");
    final String javaRuntimeId = makeId("javaRuntime");
    final String servletContainerId = makeId("servletContainer");

    presentationManager.applyPresentation(javaId, "Java version", myGroupName, null, null);
    publisher.publishStatistic(javaId, System.getProperty("java.version"));

    presentationManager.applyPresentation(javaRuntimeId, "Java runtime version", myGroupName, null, null);
    publisher.publishStatistic(javaRuntimeId, System.getProperty("java.runtime.version"));

    presentationManager.applyPresentation(servletContainerId, "Servlet container", myGroupName, null, null);
    publisher.publishStatistic(servletContainerId, myStartupContext.getServletContainerInfo());
  }

  private void publishDatabaseInfo(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String databaseId = makeId("database");
    final String jdbcId = makeId("jdbc");

    presentationManager.applyPresentation(databaseId, "Database version", myGroupName, null, null);
    publisher.publishStatistic(databaseId, myDBManager.getDatabaseProductName() + ' ' + myDBManager.getDatabaseProductVersion().toString(2,2));

    presentationManager.applyPresentation(jdbcId, "JDBC driver version", myGroupName, null, null);
    publisher.publishStatistic(jdbcId, myDBManager.getDriverName() + ' ' + myDBManager.getDriverVersion().toString(2,2));
  }

  private void publishXmx(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String maxMemoryId = makeId("maxMemory");
    presentationManager.applyPresentation(maxMemoryId, "Maximum available memory", myGroupName,
                                            new TypeBasedFormatter<Long>(Long.class) {
                                              @Override
                                              protected String doFormat(@NotNull final Long statisticValue) {
                                                return String.format("%dMb", statisticValue);
                                              }
                                            }, null);
    publisher.publishStatistic(maxMemoryId, Runtime.getRuntime().maxMemory() / MEGABYTE);
  }

  private void publishLicenseTypeAndMode(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String licenseTypeId = makeId("licenseType");
    presentationManager.applyPresentation(licenseTypeId, "License type", myGroupName, null, null);
    publisher.publishStatistic(licenseTypeId, myLicensingPolicy.isEnterpriseMode() ? "Enterprise" : "Professional");

    final LicenseMode licenseMode = new LicenseMode(myLicensingPolicy);

    final String licenseModeId = makeId("licenseMode");
    presentationManager.applyPresentation(licenseModeId, "License mode", myGroupName, new SingleValueFormatter(licenseMode.getFullDisplayName()), null);
    publisher.publishStatistic(licenseModeId, licenseMode.getKey());
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

  private void publishServerStartData(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String versionId = makeId("currentUptime");
    presentationManager.applyPresentation(versionId, "Current uptime", myGroupName, new TimeFormatter(), null);
    publisher.publishStatistic(versionId, Dates.now().getTime() - myStartupContext.getServerStartupTimestamp().getTime());
  }

  private void publishServerDistributionType(@NotNull final UsageStatisticsPublisher publisher, @NotNull final UsageStatisticsPresentationManager presentationManager) {
    final String id = makeId("distributionType");
    presentationManager.applyPresentation(id, "Distribution type", myGroupName, null, null);
    publisher.publishStatistic(id, myStartupContext.getDistributionType());
  }
}
