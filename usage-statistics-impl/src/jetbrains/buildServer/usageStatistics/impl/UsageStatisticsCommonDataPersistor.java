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

package jetbrains.buildServer.usageStatistics.impl;

import java.util.Date;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.util.BaseUsageStatisticsStatePersister;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsCommonDataPersistor extends BaseUsageStatisticsStatePersister {
  @NotNull private static final String LAST_REPORTING_DATE = "last-reporting-date";
  @NotNull private static final String REPORTING_SUGGESTION_WAS_CONSIDERED = "reporting-suggestion-was-considered";
  @Nullable private Date myLastReportingDate;
  private boolean myReportingSuggestionWasConsidered;

  public UsageStatisticsCommonDataPersistor(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    super(server, serverPaths);
  }

  @Nullable
  public Date getLastReportingDate() {
    return myLastReportingDate;
  }

  @SuppressWarnings({"NullableProblems"})
  public void setLastReportingDate(@NotNull final Date date) {
    myLastReportingDate = date;
  }

  public boolean wasReportingSuggestionConsidered() {
    return myReportingSuggestionWasConsidered;
  }

  public void markReportingSuggestionAsConsidered() {
    myReportingSuggestionWasConsidered = true;
  }

  @NotNull
  @Override
  protected String getStateName() {
    return "common";
  }

  @Override
  protected void writeExternal(@NotNull final Element element) {
    if (myLastReportingDate != null) {
      element.setAttribute(LAST_REPORTING_DATE, String.valueOf(myLastReportingDate.getTime()));
    }
    element.setAttribute(REPORTING_SUGGESTION_WAS_CONSIDERED, String.valueOf(myReportingSuggestionWasConsidered));
  }

  @Override
  protected void readExternal(@NotNull final Element element) {
    final String lastReportingDate = element.getAttributeValue(LAST_REPORTING_DATE);
    if (lastReportingDate != null) {
      try {
        myLastReportingDate = new Date(Long.parseLong(lastReportingDate));
      } catch (final NumberFormatException ignore) {}
    }
    myReportingSuggestionWasConsidered = Boolean.parseBoolean(element.getAttributeValue(REPORTING_SUGGESTION_WAS_CONSIDERED));
  }
}
