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

import java.sql.ResultSet;
import java.sql.SQLException;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SQLRunner;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.usageStatistics.Formatter;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.impl.formatters.TimeFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildDataUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider implements UsageStatisticsProvider {
  @NotNull private static final Formatter ourFormatter = new TimeFormatter();

  @NotNull private static final GenericQuery<Void> ourMainBuildDataQuery = new GenericQuery<Void>(
    "select" +
    "  count(build_id)," +
    "  sum(is_personal)," +
    "  min(remove_from_queue_time - queued_time)," +
    "  max(remove_from_queue_time - queued_time)," +
    "  avg(remove_from_queue_time - queued_time)," +
    "  min(build_finish_time_server - build_start_time_server)," +
    "  max(build_finish_time_server - build_start_time_server)," +
    "  avg(build_finish_time_server - build_start_time_server) " +
    "from (select * from history union select * from light_history) " +
    "where build_start_time_server > ?"
  );

  @NotNull private static final GenericQuery<Void> ourBuildTestCountQuery = new GenericQuery<Void>(
    "select" +
    "  min(test_count)," +
    "  max(test_count)," +
    "  avg(test_count) " +
    "from (" +
    "  select" +
    "    h.build_id, " +
    "    count(test_id) test_count " +
    "  from (select build_id, build_start_time_server from history union select build_id, build_start_time_server from light_history) h " +
    "  left outer join test_info on h.build_id = test_info.build_id " +
    "  where build_start_time_server > ? " +
    "  group by h.build_id" +
    ")"
  );

  @NotNull private final SQLRunner mySQLRunner;

  public BuildDataUsageStatisticsProvider(@NotNull final SBuildServer server) {
    mySQLRunner = server.getSQLRunner();
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final String periodDescription,
                        final long fromDate) {
    final String idFormat = "jetbrains.buildServer.usageStatistics.%sForTheLast" + periodDescription;
    final String nameFormat = "%s for the last " + periodDescription.toLowerCase();

    ourMainBuildDataQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, idFormat, nameFormat, "buildCount", "Build count", rs.getLong(1), null);
          publish(publisher, idFormat, nameFormat, "personalBuildCount", "Personal build count", rs.getLong(2), null);
          publish(publisher, idFormat, nameFormat, "minBuildWaitInQueueTime", "Minimal build waiting in queue time", getNullableLong(rs, 3), ourFormatter);
          publish(publisher, idFormat, nameFormat, "maxBuildWaitInQueueTime", "Maximal build waiting in queue time", getNullableLong(rs, 4), ourFormatter);
          publish(publisher, idFormat, nameFormat, "avgBuildWaitInQueueTime", "Average build waiting in queue time", getNullableLong(rs, 5), ourFormatter);
          publish(publisher, idFormat, nameFormat, "minBuildDuration", "Minimal build duration", getNullableLong(rs, 6), ourFormatter);
          publish(publisher, idFormat, nameFormat, "maxBuildDuration", "Maximal build duration", getNullableLong(rs, 7), ourFormatter);
          publish(publisher, idFormat, nameFormat, "avgBuildDuration", "Average build duration", getNullableLong(rs, 8), ourFormatter);
        }
        return null;
      }
    }, fromDate);

    ourBuildTestCountQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, idFormat, nameFormat, "minBuildTestCount", "Minimal test count per build", getNullableLong(rs, 1), null);
          publish(publisher, idFormat, nameFormat, "maxBuildTestCount", "Maximal test count per build", getNullableLong(rs, 2), null);
          publish(publisher, idFormat, nameFormat, "avgBuildTestCount", "Average test count per build", getNullableLong(rs, 3), null);
        }
        return null;
      }
    }, fromDate);
  }

  @Nullable
  private Long getNullableLong(final ResultSet rs, final int index) throws SQLException {
    final long value = rs.getLong(index);
    return rs.wasNull() ? null : value;
  }

  private void publish(@NotNull final UsageStatisticsPublisher publisher,
                       @NotNull final String idFormat,
                       @NotNull final String nameFormat,
                       @NotNull final String id,
                       @NotNull final String name,
                       @Nullable final Object value,
                       @Nullable final Formatter formatter) {
    publisher.publishStatistic(String.format(idFormat, id), String.format(nameFormat, name), value, formatter, null);
  }
}