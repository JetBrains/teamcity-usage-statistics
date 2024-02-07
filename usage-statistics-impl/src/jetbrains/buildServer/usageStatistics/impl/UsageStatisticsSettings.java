
package jetbrains.buildServer.usageStatistics.impl;

public class UsageStatisticsSettings {
  private boolean myIsReportingEnabled = false;

  public boolean isReportingEnabled() {
    return myIsReportingEnabled;
  }

  public void setReportingEnabled(boolean reportingEnabled) {
    myIsReportingEnabled = reportingEnabled;
  }
}