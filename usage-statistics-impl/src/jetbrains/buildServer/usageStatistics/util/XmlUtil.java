package jetbrains.buildServer.usageStatistics.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.util.ExceptionUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 15.09.2010
 */
public class XmlUtil {
  @NotNull private static final Map<String, Object> ourFileLocks = new HashMap<String, Object>();

  public static void saveXml(@NotNull final Element element, @NotNull final File file) {
    synchronized (getOrCreateLock(file)) {
      OutputStream fos = null;
      try {
        fos = new FileOutputStream(file);
        final XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(new Document(element), fos);
      }
      catch (final IOException e) {
        ExceptionUtil.rethrowAsRuntimeException(e);
      }
      finally {
        if (fos != null) {
          try {
              fos.close();
          }
          catch (final IOException e) {
            ExceptionUtil.rethrowAsRuntimeException(e);
          }
        }
      }
    }
  }

  @Nullable
  public static Element loadXml(@NotNull final File file) {
    synchronized (getOrCreateLock(file)) {
      if (!file.exists() || !file.canRead()) return null;
      try {
        return new SAXBuilder().build(file).getRootElement();
      }
      catch (final JDOMException e) {
        ExceptionUtil.rethrowAsRuntimeException(e);
      }
      catch (final IOException e) {
        ExceptionUtil.rethrowAsRuntimeException(e);
      }
    }
    return null;
  }

  @NotNull
  private synchronized static Object getOrCreateLock(@NotNull final File file) {
    final String fileName = file.getAbsolutePath();
    if (!ourFileLocks.containsKey(fileName)) {
      ourFileLocks.put(fileName, new Object());
    }
    return ourFileLocks.get(fileName);
  }
}
