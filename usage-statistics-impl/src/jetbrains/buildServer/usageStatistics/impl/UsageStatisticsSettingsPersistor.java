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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.ExceptionUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsSettingsPersistor {
  @NotNull private static final Object ourFileLock = new Object();

  @NotNull private final File mySettingsFile;

  public UsageStatisticsSettingsPersistor(@NotNull final ServerPaths serverPaths) {
    mySettingsFile = new File(serverPaths.getConfigDir(), "usage-statistics-config.xml");
  }

  public void saveSettings(@NotNull final UsageStatisticsSettings settings) {
    final Element element = new Element("usage-statistics-settings");

    element.setAttribute("reporting-enabled", String.valueOf(settings.isReportingEnabled()));

    final Date lastReportingDate = settings.getLastReportingDate();
    if (lastReportingDate != null) {
      element.setAttribute("last-reporting-date", String.valueOf(lastReportingDate.getTime()));
    }

    synchronized (ourFileLock) {
      try {
        saveXml(element);
      }
      catch (final IOException e) {
        ExceptionUtil.rethrowAsRuntimeException(e);
      }
    }
  }

  @NotNull
  public UsageStatisticsSettings loadSettings() {
    final UsageStatisticsSettings settings = new UsageStatisticsSettings();

    if (mySettingsFile.exists() && mySettingsFile.canRead()) {
      Element element = null;
      synchronized (ourFileLock) {
        try {
          element = new SAXBuilder().build(mySettingsFile).getRootElement();
        }
        catch (final JDOMException e) {
          ExceptionUtil.rethrowAsRuntimeException(e);
        }
        catch (final IOException e) {
          ExceptionUtil.rethrowAsRuntimeException(e);
        }
      }

      final String reportingEnabled = element.getAttributeValue("reporting-enabled");
      if (reportingEnabled != null) {
        settings.setReportingEnabled(Boolean.parseBoolean(reportingEnabled));
      }

      final String lastReportingDate = element.getAttributeValue("last-reporting-date");
      if (lastReportingDate != null) {
        try {
          settings.setLastReportingDate(new Date(Long.parseLong(lastReportingDate)));
        } catch (final NumberFormatException ignore) {}
      }
    }

    return settings;
  }

  private void saveXml(@NotNull final Element element) throws IOException {
    final OutputStream fos = new FileOutputStream(mySettingsFile);
    try {
      final XMLOutputter outputter = new XMLOutputter();
      outputter.setFormat(Format.getPrettyFormat());
      outputter.output(new Document(element), fos);
    }
    finally {
      fos.close();
    }
  }
}
