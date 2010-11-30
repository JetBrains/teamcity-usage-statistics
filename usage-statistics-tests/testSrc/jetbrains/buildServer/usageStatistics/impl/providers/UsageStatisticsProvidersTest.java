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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.clouds.server.CloudStatisticsProvider;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.db.TeamCityDatabaseManager;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class UsageStatisticsProvidersTest extends BaseServerTestCase {
  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFixture.addService(
      new StaticServerUsageStatisticsProvider(myServer,
                                              myFixture.getSingletonService(UserGroupManager.class),
                                              myFixture.getSingletonService(UsageStatisticsPresentationManager.class),
                                              new CloudStatisticsProvider() {
                                                public int getNumberOfProfiles() {
                                                  return 2;
                                                }

                                                public int getNumberOfImages() {
                                                  return 6;
                                                }

                                                public int getNumberOfRunningInstances() {
                                                  return 9;
                                                }
                                              }));

    myFixture.addService(
      new ServerConfigurationUsageStatisticsProvider(myServer,
                                                     new TeamCityDatabaseManager(),
                                                     myFixture.getSingletonService(UsageStatisticsPresentationManager.class))
    );
  }

  public void provider_should_not_fail_on_publishing_statistics() {
    final UsageStatisticsPublisher publisher = new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        // do nothing
      }
    };

    final Collection<UsageStatisticsProvider> providers = myServer.getExtensions(UsageStatisticsProvider.class);
    for (final UsageStatisticsProvider provider : providers) {
      provider.accept(publisher);
    }
  }

  public void static_server_usage_statistics_provider_test() {
    final Map<String, Object> statistics = collectStatisticsByProvider(StaticServerUsageStatisticsProvider.class);

    assertEquals(myServer.getProjectManager().getNumberOfProjects(), statistics.get("jb.projectNumber"));
    assertEquals(myServer.getProjectManager().getArchivedProjects().size(), statistics.get("jb.archivedProjectNumber"));
    assertEquals(myServer.getProjectManager().getNumberOfBuildTypes(), statistics.get("jb.buildTypeNumber"));

    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int agentNumber = buildAgentManager.getRegisteredAgents(true).size() + buildAgentManager.getUnregisteredAgents().size();
    assertEquals(agentNumber, statistics.get("jb.agentNumber"));

    assertEquals(myServer.getVcsManager().getAllRegisteredVcsRoots().size(), statistics.get("jb.vcsRootNumber"));
    assertEquals(myServer.getUserModel().getNumberOfRegisteredUsers(), statistics.get("jb.userNumber"));

    final UserGroupManager userGroupManager = myServer.getSingletonService(UserGroupManager.class);
    assertEquals(userGroupManager.getUserGroups().size(), statistics.get("jb.userGroupNumber"));
  }

  public void all_providers_should_be_registered() {
    final Collection<String> registeredProviders = myServer.getExtensionSources(UsageStatisticsProvider.class);

    assertTrue(registeredProviders.contains(StaticServerUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(ServerConfigurationUsageStatisticsProvider.class.getName()));
    //assertTrue(registeredProviders.contains(BuildDataUsageStatisticsProvider.class.getName()));
    //assertTrue(registeredProviders.contains(IDEUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(VCSUsageStatisticsProvider.class.getName()));
    //assertTrue(registeredProviders.contains(WebPagesUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(NotificatorUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(RunnerUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(TriggerUsageStatisticsProvider.class.getName()));
  }

  private Map<String, Object> collectStatisticsByProvider(final Class<? extends UsageStatisticsProvider> providerClass) {
    final Map<String, Object> statistics = new HashMap<String, Object>();
    final UsageStatisticsProvider provider = myServer.getSingletonService(providerClass);
    provider.accept(new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        statistics.put(id, value);
      }
    });
    return statistics;
  }
}
