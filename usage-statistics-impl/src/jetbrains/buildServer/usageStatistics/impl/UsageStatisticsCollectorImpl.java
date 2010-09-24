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

package jetbrains.buildServer.usageStatistics.impl;

import com.intellij.openapi.util.Pair;
import java.util.*;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.util.Dates;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsCollectorImpl extends BuildServerAdapter implements UsageStatisticsCollector, Runnable {
  @NotNull private static final Logger LOG = Logger.getLogger(UsageStatisticsCollectorImpl.class);
  @NotNull private static final String UPDATE_INTERVAL = "teamcity.usageStatistics.update.interval";
  private static final int DEFAULT_UPDATE_INTERVAL = 30; // 30 minutes

  @NotNull private static final String PROVIDER_SLEEP_TIME = "teamcity.usageStatistics.provider.sleep.time";
  private static final int DEFAULT_PROVIDER_SLEEP_TIME = 1000; // 1 second

  @NotNull private final ExtensionHolder myExtensionHolder;

  @NotNull private final Object myLock = new Object();

  @Nullable private List<Pair<String, Object>> myCollectedStatistics = null;
  @Nullable private Date myLastCollectingFinishDate = null;
  private boolean myIsCollectingNow = false;
  private boolean myCollectingWasForced = false;
  private boolean myServerIsActive = true;

  public UsageStatisticsCollectorImpl(@NotNull final SBuildServer server, @NotNull final ExtensionHolder extensionHolder) {
    myExtensionHolder = extensionHolder;
    server.addListener(this);
    new Thread(this, "Usage Statistics Collector").start();
  }

  public void publishCollectedStatistics(@NotNull final UsageStatisticsPublisher publisher) {
    for (final Pair<String, Object> entry : getCollectedStatistics()) {
      publisher.publishStatistic(entry.getFirst(), entry.getSecond());
    }
  }

  @NotNull
  private List<Pair<String, Object>> getCollectedStatistics() {
    synchronized (myLock) {
      if (myCollectedStatistics == null) {
        throw createIllegalStateException();
      }
      return myCollectedStatistics;
    }
  }

  public boolean isCollectingNow() {
    synchronized (myLock) {
      return myIsCollectingNow;
    }
  }

  @NotNull
  public Date getLastCollectingFinishDate() {
    if (myLastCollectingFinishDate == null) {
      throw createIllegalStateException();
    }
    return myLastCollectingFinishDate;
  }

  public boolean isStatisticsCollected() {
    synchronized (myLock) {
      return myLastCollectingFinishDate != null;
    }
  }

  public void forceAsynchronousCollectingNow() {
    if (isCollectingNow()) return;
    synchronized (myLock) {
      myCollectingWasForced = true;
      myLock.notifyAll();
    }
  }

  @Override
  public void serverShutdown() {
    synchronized (myLock) {
      myServerIsActive = false;
      myLock.notifyAll();
    }
  }

  public void run() {
    waitForUpdateInterval();

    while (serverIsActive()) {
      synchronized (myLock) {
        myIsCollectingNow = true;
        myCollectingWasForced = false;
      }

      final List<Pair<String, Object>> newStatistics = new ArrayList<Pair<String, Object>>();
      collectStatistics(newStatistics);

      synchronized (myLock) {
        myCollectedStatistics = newStatistics;
        myLastCollectingFinishDate = Dates.now();
        myIsCollectingNow = false;
      }

      waitForUpdateInterval();
    }
  }

  private void collectStatistics(@NotNull final List<Pair<String, Object>> statistics) {
    final UsageStatisticsPublisher publisher = new UsageStatisticsPublisher() {
      public void publishStatistic(@NotNull final String id, @Nullable final Object value) {
        statistics.add(Pair.create(id, value));
      }
    };

    final Collection<UsageStatisticsProvider> providers = myExtensionHolder.getExtensions(UsageStatisticsProvider.class);
    for (final UsageStatisticsProvider provider : providers) {
      collectStatisticsWithProvider(provider, publisher);
    }
  }

  private void collectStatisticsWithProvider(@NotNull final UsageStatisticsProvider provider, @NotNull final UsageStatisticsPublisher publisher) {
    try {
      provider.accept(publisher);
      Thread.sleep(getProviderSleepTime());
    }
    catch (final InterruptedException ignore) {}
    catch (final Throwable e) {
      LOG.error("Usage statistics provider failed", e);
    }
  }

  private void waitForUpdateInterval() {
    final long wakeUpTimestamp = now() + getUpdateInterval();
    while (true) {
      final long waitingTime = wakeUpTimestamp - now();
      try {
        synchronized (myLock) {
          if (!myServerIsActive || myCollectingWasForced || waitingTime <= 0) break;
          myLock.wait(waitingTime);
        }
      }
      catch (final InterruptedException ignore) {}
    }
  }

  private long now() {
    return Dates.now().getTime();
  }

  private long getUpdateInterval() {
    return TeamCityProperties.getInteger(UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL) * Dates.ONE_MINUTE;
  }

  private long getProviderSleepTime() {
    return TeamCityProperties.getLong(PROVIDER_SLEEP_TIME, DEFAULT_PROVIDER_SLEEP_TIME);
  }

  private boolean serverIsActive() {
    synchronized (myLock) {
      return myServerIsActive;
    }
  }

  @NotNull
  public static IllegalStateException createIllegalStateException() {
    return new IllegalStateException("Usage statistics were not collected yet");
  }
}
