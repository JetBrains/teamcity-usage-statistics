/*
 * Copyright 2000-2010 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import jetbrains.buildServer.serverSide.BuildServerEx;
import jetbrains.buildServer.serverSide.db.*;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import org.jetbrains.annotations.NotNull;

public class DatabaseUsageStatisticsProvider extends BaseUsageStatisticsProvider
{
  private static final String ourGroupName = "Database Info";

  @NotNull
  private final TeamCityDatabaseManager myDBManager;



  public DatabaseUsageStatisticsProvider(@NotNull final BuildServerEx server,
                                         @NotNull final TeamCityDatabaseManager dbManager,
                                         @NotNull final UsageStatisticsPresentationManager presentationManager)
  {
    super(server, presentationManager);

    myDBManager = dbManager;

    applyPresentations(presentationManager);
  }


  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publisher.publishStatistic("jb.database.type", myDBManager.getDatabaseType().humanReadableName);
    publisher.publishStatistic("jb.database.system.name", myDBManager.getDatabaseProductName());
    publisher.publishStatistic("jb.database.system.version", myDBManager.getDatabaseProductVersion());
    publisher.publishStatistic("jb.database.driver.name", myDBManager.getDriverName());
    publisher.publishStatistic("jb.database.driver.version", myDBManager.getDriverVersion());
  }


  private void applyPresentations(@NotNull final UsageStatisticsPresentationManager presentationManager) {
    presentationManager.applyPresentation("jb.database.type", "Type", ourGroupName, null);
    presentationManager.applyPresentation("jb.database.system.name", "RDBMS name", ourGroupName, null);
    presentationManager.applyPresentation("jb.database.system.version", "RDBMS version", ourGroupName, null);
    presentationManager.applyPresentation("jb.database.driver.name", "JDBC driver name", ourGroupName, null);
    presentationManager.applyPresentation("jb.database.driver.version", "JDBC driver version", ourGroupName, null);
  }
}
