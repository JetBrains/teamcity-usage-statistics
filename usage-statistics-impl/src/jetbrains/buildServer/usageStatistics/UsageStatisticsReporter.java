
package jetbrains.buildServer.usageStatistics;

public interface UsageStatisticsReporter {
  boolean reportStatistics(long statisticsExpirationPeriod);
}