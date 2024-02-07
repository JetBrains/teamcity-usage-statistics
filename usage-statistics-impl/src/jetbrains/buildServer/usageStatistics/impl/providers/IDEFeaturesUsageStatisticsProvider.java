
package jetbrains.buildServer.usageStatistics.impl.providers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.db.SQLRunnerEx;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.serverSide.impl.XmlRpcBasedRemoteServer;
import jetbrains.buildServer.serverSide.impl.XmlRpcDispatcher;
import jetbrains.buildServer.serverSide.impl.XmlRpcListener;
import jetbrains.buildServer.serverSide.impl.XmlRpcSession;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.TimeService;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IDEFeaturesUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements XmlRpcListener {

  @NotNull
  private static final String TEST_STATUS_WITH_SUCCESSFUL = "Test Status (with successful)";

  @NotNull
  private static final String TEST_STATUS_WITHOUT_SUCCESSFUL = "Test Status (without successful)";

  @NotNull
  private static final GenericQuery<Void> ourRemoteDebugSessionsCountQuery = new GenericQuery<Void>(
    "select count(*) as debug_sessions_count " +
    "from personal_vcs_history h " +
    "where scheduled_for_deletion = 0 and commit_changes = -1 and user_id is not null and change_date > ?"
  );

  @NotNull
  private static final GenericQuery<Void> ourRemoteDebugSessionUsersCountQuery = new GenericQuery<Void>(
    "select count(distinct user_id) as debug_session_users_count " +
    "from personal_vcs_history h " +
    "where scheduled_for_deletion = 0 and commit_changes = -1 and user_id is not null and change_date > ?"
  );

  @NotNull
  private final SQLRunnerEx mySQLRunner;

  @NotNull
  private final IDEUsersProvider myIdeUsersProvider;

  public IDEFeaturesUsageStatisticsProvider(@NotNull EventDispatcher<BuildServerListener> eventDispatcher,
                                            @NotNull ServerPaths serverPaths,
                                            @NotNull ServerResponsibility serverResponsibility,
                                            @NotNull SQLRunnerEx sqlRunner,
                                            @NotNull XmlRpcDispatcher xmlRpcDispatcher,
                                            @NotNull IDEUsersProvider ideUsersProvider,
                                            @NotNull TimeService timeService) {
    super(eventDispatcher, serverPaths, serverResponsibility, createDWMPeriodDescriptions(), timeService);
    mySQLRunner = sqlRunner;
    myIdeUsersProvider = ideUsersProvider;
    xmlRpcDispatcher.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.IDE_FEATURES;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    super.accept(publisher, presentationManager, periodDescription, startDate);
    publishDebugSessions(publisher, presentationManager, periodDescription, startDate);
    publishDebugSessionUsers(publisher, presentationManager, periodDescription, startDate);
  }

  @Override
  public void remoteMethodCalled(@NotNull final Class targetClass,
                                 @NotNull final String methodName,
                                 @NotNull final Vector params,
                                 @Nullable final XmlRpcSession session) {
    if (targetClass == XmlRpcBasedRemoteServer.class && session != null) {
      final Long userId = session.getAttribute(XmlRpcSession.USER_ID_ATTR, Long.class);
      if (userId != null) {
        if (methodName.endsWith(".findTests")) {
          addUsage(TEST_STATUS_WITH_SUCCESSFUL, userId);
        }
        else if (methodName.endsWith(".findFailedTests")) {
          addUsage(TEST_STATUS_WITHOUT_SUCCESSFUL, userId);
        }
      }
    }
  }

  private void publishDebugSessions(@NotNull final UsageStatisticsPublisher publisher,
                                    @NotNull final UsageStatisticsPresentationManager presentationManager,
                                    @NotNull final String periodDescription,
                                    final long fromDate) {
    final String featureName = "Remote Debug";
    final String statisticId = makeId(periodDescription, featureName) + ".sessions";
    presentationManager.applyPresentation(statisticId, featureName + " (sessions)", myGroupName, null, null);
    ourRemoteDebugSessionsCountQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      @Override
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publisher.publishStatistic(statisticId, rs.getInt(1));
        }
        return null;
      }
    }, fromDate);
  }

  private void publishDebugSessionUsers(@NotNull final UsageStatisticsPublisher publisher,
                                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                                        @NotNull final String periodDescription,
                                        final long fromDate) {
    final String featureName = "Remote Debug";
    final String statisticId = makeId(periodDescription, featureName);
    presentationManager.applyPresentation(statisticId, featureName, myGroupName, new PercentageFormatter(getTotalUsersCount(fromDate)), getValueTooltip());
    ourRemoteDebugSessionUsersCountQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publisher.publishStatistic(statisticId, rs.getInt(1));
        }
        return null;
      }
    }, fromDate);
  }

  @Override
  protected int getTotalUsersCount(final long startDate) {
    return myIdeUsersProvider.getIDEUsers(startDate).size();
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "ideFeaturesUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "feature";
  }

  @NotNull
  @Override
  protected String getToolIdName() {
    return "id";
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String toolId) {
    return true;
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of IDE users)";
  }
}