/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.util;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Helper for persisting the state of the TeamCity plugin.
 */
public abstract class BasePluginStatePersister extends BuildServerAdapter {
  @NotNull private static final Logger LOG = Logger.getLogger(BasePluginStatePersister.class);

  @NotNull private final File myFile;
  private final ServerResponsibility myServerResponsibility;

  protected BasePluginStatePersister(@NotNull final EventDispatcher<BuildServerListener> eventDispatcher,
                                     @NotNull final ServerPaths serverPaths,
                                     @NotNull final ServerResponsibility serverResponsibility) {
    myFile = new File(getDataDir(serverPaths), getStateName() + ".xml");
    myServerResponsibility = serverResponsibility;
    eventDispatcher.addListener(this);
  }

  @NotNull
  protected abstract String getPluginName();

  @NotNull
  protected abstract String getStateName();

  protected abstract void writeExternal(@NotNull Element element);

  protected abstract void readExternal(@NotNull Element element);

  @Override
  public void serverStartup() {
    loadState();
  }

  @Override
  public void serverShutdown() {
    if (myServerResponsibility.canManageServerConfig()) {
      saveState();
    }
  }

  @NotNull
  private File getDataDir(@NotNull final ServerPaths serverPaths) {
    try {
      return FileUtil.createDir(new File(serverPaths.getPluginDataDirectory(), getPluginName()));
    }
    catch (final IOException e) {
      ExceptionUtil.rethrowAsRuntimeException(e);
      return null;
    }
  }

  protected void saveState() {
    try {
      final Element root = new Element(getRootElementName());
      writeExternal(root);
      FileUtil.writeFile(myFile, XmlUtil.to_s(root), "UTF-8");
    }
    catch (Exception e) {
      LOG.error("Failed to write \"" + getPluginName() + "\" plugin state to the \"" + myFile.getAbsolutePath() + "\" file: " + e, e);
    }
  }

  @NotNull
  protected String getRootElementName() {
    return "root";
  }

  protected void loadState() {
    if (!myFile.isFile()) return;
    try {
      final Element root = XmlUtil.from_s(FileUtil.readText(myFile, "UTF-8"));
      readExternal(root);
    }
    catch (Exception e) {
      LOG.error("Failed to read \"" + getPluginName() + "\" plugin state from the \"" + myFile.getAbsolutePath() + "\" file: " + e, e);
    }
  }
}
