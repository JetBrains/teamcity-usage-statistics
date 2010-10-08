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
import java.util.Collections;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.SQLRunner;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TimeFormatter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildDataUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider {
  @NotNull private static final UsageStatisticsFormatter ourTimeFormatter = new TimeFormatter();

  @NotNull private static final GenericQuery<Void> ourMainBuildDataQuery = new GenericQuery<Void>(
    "select" +
    "  count(h.build_id)," +
    "  sum(h.is_personal)," +
    "  avg(h.remove_from_queue_time - h.queued_time)," +
    "  avg(h.build_finish_time_server - h.build_start_time_server) " +
    "from (select * from history union select * from light_history) h " +
    "where h.build_finish_time_server > ?"
  );

  @NotNull private static final GenericQuery<Void> ourBuildTestCountQuery = new GenericQuery<Void>(
    "select" +
    "  max(t.test_count) " +
    "from (" +
    "  select" +
    "    h.build_id, " +
    "    count(ti.test_id) test_count " +
    "  from (" +
    "    select build_id, build_finish_time_server from history " +
    "    union " +
    "    select build_id, build_finish_time_server from light_history" +
    "  ) h " +
    "  left outer join test_info ti on h.build_id = ti.build_id " +
    "  where h.build_finish_time_server > ? " +
    "  group by h.build_id" +
    ") t"
  );

  @NonNls @NotNull private static final String BUILD_USAGE_GROUP = "Builds Usage";

  @NotNull private final SQLRunner mySQLRunner;

  public BuildDataUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                          @NotNull final UsageStatisticsPresentationManager presentationManager,
                                          @NotNull final PluginDescriptor pluginDescriptor) {
    super(server, presentationManager, pluginDescriptor, Collections.singleton(BUILD_USAGE_GROUP));
    mySQLRunner = server.getSQLRunner();
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final String periodDescription,
                        final long fromDate) {
    final String idFormat = createIdFromat(periodDescription);

    ourMainBuildDataQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, idFormat, "buildCount", rs.getLong(1));
          publish(publisher, idFormat, "personalBuildCount", rs.getLong(2));
          publish(publisher, idFormat, "avgBuildWaitInQueueTime", getNullableLong(rs, 3));
          publish(publisher, idFormat, "avgBuildDuration", getNullableLong(rs, 4));
        }
        return null;
      }
    }, fromDate);

    ourBuildTestCountQuery.execute(mySQLRunner, new GenericQuery.ResultSetProcessor<Void>() {
      public Void process(final ResultSet rs) throws SQLException {
        if (rs.next()) {
          publish(publisher, idFormat, "maxBuildTestCount", getNullableLong(rs, 1));
        }
        return null;
      }
    }, fromDate);
  }

  @Override
  protected boolean mustSortStatistics() {
    return false;
  }

  @Override
  protected void applyPresentations(@NotNull final UsageStatisticsPresentationManager manager, @NotNull final String periodDescription) {
    final String idFormat = createIdFromat(periodDescription);

    apply(manager, idFormat, "buildCount", "Build count", null);
    apply(manager, idFormat, "personalBuildCount", "Personal build count", null);
    apply(manager, idFormat, "avgBuildWaitInQueueTime", "Average build waiting in queue time", ourTimeFormatter);
    apply(manager, idFormat, "avgBuildDuration", "Average build duration", ourTimeFormatter);
    apply(manager, idFormat, "maxBuildTestCount", "Maximum test count per build", null);
  }

  @NotNull
  private String createIdFromat(@NotNull final String periodDescription) {
    return "jb.%s." + periodDescription.toLowerCase();
  }

  @Nullable
  private Long getNullableLong(final ResultSet rs, final int index) throws SQLException {
    final long value = rs.getLong(index);
    return rs.wasNull() ? null : value;
  }

  private void apply(@NotNull final UsageStatisticsPresentationManager presentationManager,
                     @NotNull final String idFormat,
                     @NotNull final String id,
                     @NotNull final String name,
                     @Nullable final UsageStatisticsFormatter formatter) {
    presentationManager.applyPresentation(String.format(idFormat, id), name, BUILD_USAGE_GROUP, formatter);
  }

  private void publish(@NotNull final UsageStatisticsPublisher publisher,
                       @NotNull final String idFormat,
                       @NotNull final String id,
                       @Nullable final Object value) {
    publisher.publishStatistic(String.format(idFormat, id), value);
  }
}