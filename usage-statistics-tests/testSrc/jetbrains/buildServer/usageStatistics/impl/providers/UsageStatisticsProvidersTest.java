package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

/**
 * @author Maxim.Manuylov
 *         Date: 16.09.2010
 */
@Test
public class UsageStatisticsProvidersTest extends BaseServerTestCase {
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

    assertEquals(myServer.getProjectManager().getNumberOfProjects(), statistics.get("jetbrains.buildServer.usageStatistics.projectNumber"));
    assertEquals(myServer.getProjectManager().getArchivedProjects().size(), statistics.get("jetbrains.buildServer.usageStatistics.archivedProjectNumber"));
    assertEquals(myServer.getProjectManager().getNumberOfBuildTypes(), statistics.get("jetbrains.buildServer.usageStatistics.buildTypeNumber"));

    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int agentNumber = buildAgentManager.getRegisteredAgents(true).size() + buildAgentManager.getUnregisteredAgents().size();
    assertEquals(agentNumber, statistics.get("jetbrains.buildServer.usageStatistics.agentNumber"));

    assertEquals(myServer.getVcsManager().getAllRegisteredVcsRoots().size(), statistics.get("jetbrains.buildServer.usageStatistics.vcsRootNumber"));
    assertEquals(myServer.getUserModel().getNumberOfRegisteredUsers(), statistics.get("jetbrains.buildServer.usageStatistics.userNumber"));

    final UserGroupManager userGroupManager = myServer.getSingletonService(UserGroupManager.class);
    assertEquals(userGroupManager.getUserGroups().size(), statistics.get("jetbrains.buildServer.usageStatistics.userGroupNumber"));
  }

  public void all_providers_should_be_registered() {
    final Collection<String> registeredProviders = myServer.getExtensionSources(UsageStatisticsProvider.class);

    assertTrue(registeredProviders.contains(StaticServerUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(BuildDataUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(IDEUsageStatisticsProvider.class.getName()));
    assertTrue(registeredProviders.contains(VCSUsageStatisticsProvider.class.getName()));
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
