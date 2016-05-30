/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import jetbrains.buildServer.serverSide.BuildAttributes;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.db.DBAction;
import jetbrains.buildServer.serverSide.db.DBException;
import jetbrains.buildServer.serverSide.db.DBFunctions;
import jetbrains.buildServer.serverSide.db.SQLRunnerEx;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.serverSide.versionedSettings.VersionedSettingsManager;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.vcs.SVcsRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VersionedSettingsBuildsUsagesStatisticsProvider extends BaseDynamicUsageStatisticsProvider {

  private static final GenericQuery<Map<String, Integer>> BUILDS_WITH_SETTINGS = new GenericQuery<Map<String, Integer>>(
    "select bs.build_type_id, count(*) " +
    "from build_state bs " +
    "inner join build_attrs attr on attr.build_state_id = bs.id " +
    "where attr.attr_name = '" + BuildAttributes.HAS_FROZEN_SETTINGS + "' " +
    "and (attr.attr_value = 'true' or attr.attr_value = 'vcs') " +
    "and queued_time > ? " +
    "group by bs.build_type_id",
    new GenericQuery.ResultSetProcessor<Map<String, Integer>>() {
      @Nullable
      public Map<String, Integer> process(final ResultSet rs) throws SQLException {
        Map<String, Integer> result = new HashMap<String, Integer>();
        while (rs.next()) {
          result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
      }
    }
  );

  private final ProjectManager myProjectManager;
  private final VersionedSettingsManager myVersionedSettingsManager;
  private final SQLRunnerEx mySql;


  public VersionedSettingsBuildsUsagesStatisticsProvider(@NotNull ProjectManager projectManager,
                                                         @NotNull VersionedSettingsManager versionedSettingsManager,
                                                         @NotNull SQLRunnerEx sqlRunner) {
    super(createDWMPeriodDescriptions(), "0");
    myProjectManager = projectManager;
    myVersionedSettingsManager = versionedSettingsManager;
    mySql = sqlRunner;
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher,
                        @NotNull final UsageStatisticsPresentationManager presentationManager,
                        @NotNull final String periodDescription,
                        final long startDate) {
    Map<String, Integer> buildTypeBuildsCount = mySql.runAndRetry(new DBAction<Map<String, Integer>>() {
      public Map<String, Integer> run(final DBFunctions dbf) throws DBException {
        return BUILDS_WITH_SETTINGS.execute(dbf, startDate);
      }
    });

    Map<String, Integer> vcs2buildCount = new HashMap<String, Integer>();
    Map<SProject, AtomicReference<SVcsRoot>> projectSettingsRootCache = new HashMap<SProject, AtomicReference<SVcsRoot>>();
    int total = 0;
    for (Map.Entry<String, Integer> e : buildTypeBuildsCount.entrySet()) {
      SBuildType bt = myProjectManager.findBuildTypeById(e.getKey());
      if (bt == null)
        continue;

      SVcsRoot root = getSettingsRoot(projectSettingsRootCache, bt.getProject());
      if (root == null)
        continue;

      int count = e.getValue();
      aggregateBuildCount(vcs2buildCount, root.getVcsDisplayName(), count);
      total += count;
    }

    final UsageStatisticsFormatter formatter = new PercentageFormatter(total);
    for (Map.Entry<String, Integer> e : vcs2buildCount.entrySet()) {
      String statId = makeId(e.getKey(), periodDescription);
      presentationManager.applyPresentation(statId, e.getKey(), myGroupName, formatter, "Builds count (% of builds with settings from VCS during the period)");
      publisher.publishStatistic(statId, e.getValue());
    }
  }

  private void aggregateBuildCount(@NotNull Map<String, Integer> vcs2buildCount, @NotNull String vcsName, int increment) {
    Integer count = vcs2buildCount.get(vcsName);
    if (count == null)
      count = 0;
    vcs2buildCount.put(vcsName, count + increment);
  }


  @Nullable
  private SVcsRoot getSettingsRoot(@NotNull Map<SProject, AtomicReference<SVcsRoot>> cache, @NotNull SProject project) {
    AtomicReference<SVcsRoot> cached = cache.get(project);
    if (cached == null) {
      cached = new AtomicReference<SVcsRoot>(myVersionedSettingsManager.getVersionedSettingsVcsRoot(project));
      cache.put(project, cached);
    }
    return cached.get();
  }

  @Override
  protected boolean mustSortStatistics() {
    return true;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.VCS_FEATURES;
  }
}
