/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsCollector;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationProvider;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.NamedDaemonThreadFactory;
import jetbrains.buildServer.util.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UsageStatisticsCollectorImpl extends BuildServerAdapter implements UsageStatisticsCollector, Runnable {
  @NotNull private static final Logger LOG = Logger.getInstance(UsageStatisticsCollectorImpl.class.getName());

  @NotNull private static final String PROVIDER_SLEEP_TIME = "teamcity.usageStatistics.provider.sleep.time.milliseconds";
  private static final int DEFAULT_PROVIDER_SLEEP_TIME = 1000; // 1 second

  @NotNull private final ExtensionHolder myExtensionHolder;
  @NotNull private final UsageStatisticsPresentationManager myPresentationManager;

  @NotNull private final Object myLock = new Object();

  @Nullable private List<Pair<String, Object>> myCollectedStatistics = null;
  @Nullable private Date myLastCollectingFinishDate = null;
  private boolean myIsCollectingNow = false;
  private boolean myCollectingWasForced = false;
  private boolean myServerIsActive = true;

  public UsageStatisticsCollectorImpl(@NotNull final SBuildServer server,
                                      @NotNull final UsageStatisticsPresentationManager presentationManager) {
    myExtensionHolder = server;
    myPresentationManager = presentationManager;
    server.addListener(this);

    new NamedDaemonThreadFactory("Usage statistics collector").newThread(this).start();
  }

  public void publishCollectedStatistics(@NotNull final UsageStatisticsPublisher publisher) {
    for (final Pair<String, Object> entry : getCollectedStatistics()) {
      publisher.publishStatistic(entry.getFirst(), entry.getSecond());
    }
  }

  @SuppressWarnings("NullableProblems")
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

  @SuppressWarnings("NullableProblems")
  @NotNull
  public Date getLastCollectingFinishDate() {
    synchronized (myLock) {
      if (myLastCollectingFinishDate == null) {
        throw createIllegalStateException();
      }
      return myLastCollectingFinishDate;
    }
  }

  public boolean isStatisticsCollected() {
    synchronized (myLock) {
      return myLastCollectingFinishDate != null;
    }
  }

  public void forceAsynchronousCollectingNow() {
    synchronized (myLock) {
      if (myIsCollectingNow) return;
      myCollectingWasForced = true;
      myLock.notifyAll();
    }
  }

  public void collectStatisticsAndWait() {
    synchronized (myLock) {
      final Date oldLastCollectingFinishDate = myLastCollectingFinishDate;
      forceAsynchronousCollectingNow();
      while (myLastCollectingFinishDate == oldLastCollectingFinishDate) {
        try {
          myLock.wait();
        } catch (final InterruptedException ignore) {}
      }
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
    waitForEvent();

    while (serverIsActive()) {
      synchronized (myLock) {
        myIsCollectingNow = true;
        myCollectingWasForced = false;
      }
      LOG.info("Starting usage statistics collection...");
      final long processStart = System.nanoTime();

      final List<Pair<String, Object>> newStatistics = new ArrayList<Pair<String, Object>>();
      collectStatistics(newStatistics);

      synchronized (myLock) {
        myCollectedStatistics = newStatistics;
        myLastCollectingFinishDate = Dates.now();
        myIsCollectingNow = false;
        myLock.notifyAll();
      }
      final long processFinish = System.nanoTime();
      LOG.info("Finish usage statistics collection, collected in " + TimeUnit.MILLISECONDS.convert(processFinish - processStart, TimeUnit.NANOSECONDS) + " ms");
      waitForEvent();
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

    final Collection<UsageStatisticsPresentationProvider> presentationProviders = myExtensionHolder.getExtensions(UsageStatisticsPresentationProvider.class);
    for (final UsageStatisticsPresentationProvider presentationProvider : presentationProviders) {
      applyPresentationsWithProvider(presentationProvider);
    }
  }

  private void collectStatisticsWithProvider(@NotNull final UsageStatisticsProvider provider, @NotNull final UsageStatisticsPublisher publisher) {
    try {
      NamedThreadFactory.executeWithNewThreadName("Collecting usage statistics with provider " + provider.toString(), new Runnable() {
        public void run() {
          provider.accept(publisher);
        }
      });
      //Thread.sleep(getProviderSleepTime());
    }
    //catch (final InterruptedException ignore) {}
    catch (final Throwable e) {
      LOG.infoAndDebugDetails("Usage statistics provider " + provider.toString() + " failed", e);
    }
  }

  private void applyPresentationsWithProvider(@NotNull final UsageStatisticsPresentationProvider presentationProvider) {
    try {
      presentationProvider.accept(myPresentationManager);
      //Thread.sleep(getProviderSleepTime());
    }
    //catch (final InterruptedException ignore) {}
    catch (final Throwable e) {
      LOG.infoAndDebugDetails("Usage statistics presentation provider " + presentationProvider.toString() + " failed", e);
    }
  }

  private void waitForEvent() {
    while (true) {
      try {
        synchronized (myLock) {
          if (!myServerIsActive || myCollectingWasForced) break;
          myLock.wait();
        }
      }
      catch (final InterruptedException ignore) {}
    }
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
    return new IllegalStateException("Usage statistics data was not collected yet");
  }
}
