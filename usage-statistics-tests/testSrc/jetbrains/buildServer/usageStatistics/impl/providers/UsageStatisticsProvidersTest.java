/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import java.util.*;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.db.TestDB;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.updates.TeamCityUpdater;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class UsageStatisticsProvidersTest extends BaseServerTestCase {
  
  private Collection<BaseUsageStatisticsProvider> myProviders;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myProviders = new ArrayList<BaseUsageStatisticsProvider>();
    myProviders.add(new IDEUsageStatisticsProvider(myServer, myFixture.getServerPaths(), myFixture.getXmlRpcDispatcher(), myFixture.getSingletonService(TimeService.class)));
    myProviders.add(new NotificatorUsageStatisticsProvider(myServer, myFixture.getNotificatorRegistry(), myFixture.getNotificationRulesManager()));
    myProviders.add(new RunnerUsageStatisticsProvider(myServer));
    myProviders.add(getServerConfigurationUsageStatisticsProvider());
    myProviders.add(getStaticServerUsageProvider());
    myProviders.add(new TriggerUsageStatisticsProvider(myServer));
    myProviders.add(new VCSUsageStatisticsProvider(myServer));
  }

  @NotNull
  private ServerConfigurationUsageStatisticsProvider getServerConfigurationUsageStatisticsProvider() {
    return new ServerConfigurationUsageStatisticsProvider(TestDB.getDbManager(), myFixture.getLoginConfiguration(),
                                                          myFixture.getServer(), myFixture.getServerSettings(), myFixture.getAuditLogProvider(), myFixture.getStartupContext());
  }

  @NotNull
  private StaticServerUsageStatisticsProvider getStaticServerUsageProvider() {
    myProviders.add(new AgentsJavaUsageStatisticsProvider(myServer));
    myProviders.add(new AgentsPlatformUsageStatisticsProvider(myServer));
    myProviders.add(new ServerLoadUsageStatisticsProvider(myServer, new WebUsersProvider() {
      @NotNull
      public Set<String> getWebUsers(final long fromTimestamp) {
        return Collections.emptySet();
      }
    }, new IDEUsersProvider() {
      @NotNull
      public Set<String> getIDEUsers(final long fromTimestamp) {
        return Collections.emptySet();
      }
    }));
    return new StaticServerUsageStatisticsProvider(myServer, myFixture.getUserGroupManager(), myFixture.getAgentPoolManager());
  }

  public void provider_should_not_fail_on_publishing_statistics() {
    final UsageStatisticsPublisher publisher = new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        // do nothing
      }
    };

    for (final BaseUsageStatisticsProvider provider : myProviders) {
      provider.setIdFormat("");
      provider.setGroupName("");
      provider.accept(publisher);
    }
  }

  public void static_server_usage_statistics_provider_test() {
    final StaticServerUsageStatisticsProvider provider = getStaticServerUsageProvider();
    provider.setIdFormat("%s");

    final Map<String, Object> statistics = collectStatisticsByProvider(provider);

    assertEquals(myServer.getProjectManager().getNumberOfProjects(), statistics.get("projectNumber"));
    assertEquals(myServer.getProjectManager().getArchivedProjects().size(), statistics.get("archivedProjectNumber"));
    assertEquals(myServer.getProjectManager().getNumberOfBuildTypes(), statistics.get("buildTypeNumber"));

    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    assertEquals(buildAgentManager.getRegisteredAgents(true).size(), statistics.get("allRegisteredAgentNumber"));
    assertEquals(buildAgentManager.getRegisteredAgents(false).size(), statistics.get("authorizedRegisteredAgentNumber"));

    assertEquals(myServer.getVcsManager().getAllRegisteredVcsRoots().size(), statistics.get("vcsRootNumber"));
    assertEquals(myServer.getUserModel().getNumberOfRegisteredUsers(), statistics.get("userNumber"));

    final UserGroupManager userGroupManager = myServer.getSingletonService(UserGroupManager.class);
    assertEquals(userGroupManager.getUserGroups().size(), statistics.get("userGroupNumber"));
  }

  public void server_id_test() {
    final ServerConfigurationUsageStatisticsProvider provider = getServerConfigurationUsageStatisticsProvider();
    provider.setIdFormat("%s");

    final Map<String, Object> statistics = collectStatisticsByProvider(provider);

    assertEquals(myServer.getSingletonService(ServerSettings.class).getServerUUID(), statistics.get("id"));
  }

  public void auto_updates_count() {
    makeLoggedIn(createAdmin("admin"));
    final ServerConfigurationUsageStatisticsProvider provider = getServerConfigurationUsageStatisticsProvider();
    provider.setIdFormat("%s");

    Map<String, Object> statistics = collectStatisticsByProvider(provider);
    assertEquals(0, statistics.get("upgradesCount"));

    new TeamCityUpdater.LogUpdateToAudit(myFixture.getAuditLogFactory(), "2019.1").run();
    statistics = collectStatisticsByProvider(provider);
    assertEquals(1, statistics.get("upgradesCount"));

    new TeamCityUpdater.LogUpdateToAudit(myFixture.getAuditLogFactory(), "2019.2").run();
    statistics = collectStatisticsByProvider(provider);
    assertEquals(2, statistics.get("upgradesCount"));
  }

  private Map<String, Object> collectStatisticsByProvider(@NotNull final UsageStatisticsProvider provider) {
    final Map<String, Object> statistics = new HashMap<String, Object>();
    provider.accept(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        statistics.put(id, value);
      }
    });
    return statistics;
  }
}
