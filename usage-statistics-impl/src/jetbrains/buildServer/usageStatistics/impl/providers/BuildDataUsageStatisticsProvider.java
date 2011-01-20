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

import java.sql.ResultSet;
import java.sql.SQLException;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SQLRunner;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TimeFormatter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildDataUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private static final UsageStatisticsFormatter ourTimeFormatter = new TimeFormatter();

  @NotNull private static final GenericQuery<Void> ourMainBuildDataQuery = new GenericQuery<Void>(
    "select" +
    "  count(h.build_id) as build_count," +
    "  sum(h.is_personal) as personal_build_count," +
    "  avg(h.remove_from_queue_time - h.queued_time) as avg_build_queue_time," +
    "  avg(h.build_finish_time_server - h.build_start_time_server) as avg_build_duration " +
    "from (" +
    "  select *" +
    "    from history " +
    "    where build_finish_time_server > ?" +
    "  union all" +
    "  select *" +
    "    from light_history" +
    "    where build_finish_time_server > ?" +
    ") h"
  );

  @NotNull private static final GenericQuery<Void> ourBuildTestCountQuery = new GenericQuery<Void>(
    "select" +
    "  max(t.test_count) as max_test_count " +
    "from (" +
    "  select" +
    "    count(*) as test_count " +
    "  from (" +
    "    select build_id " +
    "      from history " +
    "      where build_finish_time_server > ? " +
    "    union all" +
    "    select build_id " +
    "      from light_history" +
    "      where build_finish_time_server > ? " +
    "    ) h " +
    "    join test_info ti on h.build_id = ti.build_id " +
    "  group by h.build_id" +
    ") t"
  );

  @NotNull private final SQLRunner mySQLRunner;

  public BuildDataUsageStatisticsProvider(@NotNull final SBuildServer server,
                                          @NotNull final UsageStatisticsPresentationManager presentationManager,
                                          @NotNull final PluginDescriptor pluginDescriptor) {
    super(presentationManager, pluginDescriptor);
    mySQLRunner = server.getSQLRunner();
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final String periodDescription,
                        final long fromDate) {
    applyPresentations(periodDescription);

    ourMainBuildDataQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
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

    ourBuildTestCountQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, periodDescription, "maxBuildTestCount", getNullableLong(rs, 1));
        }
        return null;
      }
    }, fromDate, fromDate);
  }

  @Override
  protected boolean mustSortStatistics() {
    return false;
  }

  private void applyPresentations(@NotNull final String periodDescription) {
    apply(periodDescription, "buildCount", "Build count", null);
    apply(periodDescription, "personalBuildCount", "Personal build count", null);
    apply(periodDescription, "avgBuildWaitInQueueTime", "Average build waiting in queue time", ourTimeFormatter);
    apply(periodDescription, "avgBuildDuration", "Average build duration", ourTimeFormatter);
    apply(periodDescription, "maxBuildTestCount", "Maximum test count per build", null);
  }

  @Nullable
  private Long getNullableLong(final ResultSet rs, final int index) throws SQLException {
    final long value = rs.getLong(index);
    return rs.wasNull() ? null : value;
  }

  private void apply(@NotNull final String periodDescription,
                     @NotNull final String id,
                     @NotNull final String name,
                     @Nullable final UsageStatisticsFormatter formatter) {
    myPresentationManager.applyPresentation(makeId(id, periodDescription), name, myGroupName, formatter);
  }

  private void publish(@NotNull final UsageStatisticsPublisher publisher,
                       @NotNull final String periodDescription,
                       @NotNull final String id,
                       @Nullable final Object value) {
    publisher.publishStatistic(makeId(id, periodDescription), value);
  }
}