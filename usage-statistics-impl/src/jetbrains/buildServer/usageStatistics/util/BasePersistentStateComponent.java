package jetbrains.buildServer.usageStatistics.util;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public abstract class BasePersistentStateComponent extends BuildServerAdapter {
  @NotNull protected final SBuildServer myServer;
  @NotNull private final File myFile;

  public BasePersistentStateComponent(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    myServer = server;
    myFile = new File(getDataDir(serverPaths), getId() + ".xml");
    myServer.addListener(this);
    loadState();
  }

  @Override
  public void serverShutdown() {
    saveState();
  }

  @NotNull
  protected abstract String getId();

  protected abstract void writeExternal(@NotNull Element element);

  protected abstract void readExternal(@NotNull Element element);

  @NotNull
  private File getDataDir(@NotNull final ServerPaths serverPaths) {
    try {
      return FileUtil.createDir(new File(serverPaths.getPluginDataDirectory(), "usage-statistics"));
    }
    catch (final IOException e) {
      ExceptionUtil.rethrowAsRuntimeException(e);
      //noinspection ConstantConditions
      return null;
    }
  }

  private void saveState() {
    final Element root = new Element("root");
    writeExternal(root);
    XmlUtil.saveXml(root, myFile);
  }

  private void loadState() {
    final Element root = XmlUtil.loadXml(myFile);
    if (root != null) {
      readExternal(root);
    }
  }
}
