/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TimeFormatter;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerLoadUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private static final UsageStatisticsFormatter ourTimeFormatter = new TimeFormatter();

  @NotNull private static final GenericQuery<Void> ourMainBuildDataQuery = new GenericQuery<Void>(
    "select" +
     " count(h.build_id) as build_count," +
     " sum(h.is_personal) as personal_build_count," +
     " avg(h.remove_from_queue_time - h.queued_time) as avg_build_queue_time," +
     " avg(h.build_finish_time_server - h.build_start_time_server) as avg_build_duration" +
    " from (" +
    " select history.build_id, history.is_personal,build_finish_time_server,build_start_time_server, history.remove_from_queue_time as remove_from_queue_time, history.queued_time as queued_time" +
     " from history where build_finish_time_server > ?" +
    " union all" +
    " select build_id, is_personal,build_finish_time_server,build_start_time_server, remove_from_queue_time, queued_time" +
     " from light_history" +
     " where build_finish_time_server > ?" +
    ") h"
  );

  @NotNull private static final GenericQuery<Void> ourBuildTestCountQuery = new GenericQuery<Void>(
    "select" +
    " max(s.test_count) as max_test_count" +
    " from stats s" +
    " inner join history h on s.build_id = h.build_id" +
    " where h.build_finish_time_server > ?"
  );

  @NotNull private static final GenericQuery<Void> ourVcsChangesCountQuery = new GenericQuery<Void>(
    "select count(*) as vcs_changes_count" +
    " from vcs_history h" +
    " where register_date > ?"
  );

  @NotNull private final BuildServerEx myServer;
  @NotNull private final WebUsersProvider myWebUsersProvider;
  @NotNull private final IDEUsersProvider myIDEUsersProvider;

  public ServerLoadUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                           @NotNull final WebUsersProvider webUsersProvider,
                                           @NotNull final IDEUsersProvider ideUsersProvider) {
    super(createDWMPeriodDescriptions(), null);
    myServer = server;
    myWebUsersProvider = webUsersProvider;
    myIDEUsersProvider = ideUsersProvider;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.SERVER_LOAD;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    publishBuildData(publisher, presentationManager, periodDescription, startDate);
    publishOnlineUsers(publisher, presentationManager, periodDescription, startDate);
    publishVcsChanges(publisher, presentationManager, periodDescription, startDate);
  }

  private void publishBuildData(@NotNull final UsageStatisticsPublisher publisher,
                                @NotNull final UsageStatisticsPresentationManager presentationManager,
                                @NotNull final String periodDescription,
                                final long fromDate) {
    apply(presentationManager, periodDescription, "buildCount", "Build count", null, null);
    apply(presentationManager, periodDescription, "personalBuildCount", "Personal build count", null, null);
    apply(presentationManager, periodDescription, "avgBuildWaitInQueueTime", "Average build waiting in queue time", ourTimeFormatter, null);
    apply(presentationManager, periodDescription, "avgBuildDuration", "Average build duration", ourTimeFormatter, null);
    apply(presentationManager, periodDescription, "maxBuildTestCount", "Maximum test count per build", null, null);

    ourMainBuildDataQuery.execute(myServer.getSQLRunner(), new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, periodDescription, "buildCount", rs.getLong(1));
          publish(publisher, periodDescription, "personalBuildCount", rs.getLong(2));
          publish(publisher, periodDescription, "avgBuildWaitInQueueTime", getNullableLong(rs, 3));
          publish(publisher, periodDescription, "avgBuildDuration", getNullableLong(rs, 4));
        }
        return null;
      }
    }, fromDate, fromDate);

    ourBuildTestCountQuery.execute(myServer.getSQLRunner(), new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, periodDescription, "maxBuildTestCount", getNullableLong(rs, 1));
        }
        return null;
      }
    }, fromDate);
  }

  private void publishOnlineUsers(@NotNull final UsageStatisticsPublisher publisher,
                                  @NotNull final UsageStatisticsPresentationManager presentationManager,
                                  @NotNull final String periodDescription,
                                  final long fromDate) {
    final String webUsersId = "webUsers";
    final String ideUsersId = "ideUsers";
    final String webOnlyUsersId = "webOnlyUsers";
    final String ideOnlyUsersId = "ideOnlyUsers";

    final UsageStatisticsFormatter formatter = new PercentageFormatter(myServer.getUserModel().getNumberOfRegisteredUsers());
    final String valueTooltip = "User count (% of all users)";

    apply(presentationManager, periodDescription, webUsersId, "Web users", formatter, valueTooltip);
    apply(presentationManager, periodDescription, ideUsersId, "IDE users", formatter, valueTooltip);
    apply(presentationManager, periodDescription, webOnlyUsersId, "Web only users", formatter, valueTooltip);
    apply(presentationManager, periodDescription, ideOnlyUsersId, "IDE only users", formatter, valueTooltip);

    final Set<String> webUsers = myWebUsersProvider.getWebUsers(fromDate);
    final Set<String> ideUsers = myIDEUsersProvider.getIDEUsers(fromDate);

    publish(publisher, periodDescription, webUsersId, webUsers.size());
    publish(publisher, periodDescription, ideUsersId, ideUsers.size());
    publish(publisher, periodDescription, webOnlyUsersId, CollectionsUtil.minus(webUsers, ideUsers).size());
    publish(publisher, periodDescription, ideOnlyUsersId, CollectionsUtil.minus(ideUsers, webUsers).size());
  }

  private void publishVcsChanges(@NotNull final UsageStatisticsPublisher publisher,
                                 @NotNull final UsageStatisticsPresentationManager presentationManager,
                                 @NotNull final String periodDescription,
                                 final long fromDate) {
    final String vcsChangesId = "vcsChanges";
    apply(presentationManager, periodDescription, vcsChangesId, "VCS changes", null, null);
    ourVcsChangesCountQuery.execute(myServer.getSQLRunner(), new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, periodDescription, vcsChangesId, rs.getInt(1));
        }
        return null;
      }
    }, fromDate);
  }

  @Override
  protected boolean mustSortStatistics() {
    return false;
  }

  @Nullable
  private Long getNullableLong(final ResultSet rs, final int index) throws SQLException {
    final long value = rs.getLong(index);
    return rs.wasNull() ? null : value;
  }

  private void apply(@NotNull final UsageStatisticsPresentationManager presentationManager,
                     @NotNull final String periodDescription,
                     @NotNull final String id,
                     @NotNull final String name,
                     @Nullable final UsageStatisticsFormatter formatter,
                     @Nullable final String valueTooltip) {
    presentationManager.applyPresentation(makeId(id, periodDescription), name, myGroupName, formatter, valueTooltip);
  }

  private void publish(@NotNull final UsageStatisticsPublisher publisher,
                       @NotNull final String periodDescription,
                       @NotNull final String id,
                       @Nullable final Object value) {
    publisher.publishStatistic(makeId(id, periodDescription), value);
  }
}