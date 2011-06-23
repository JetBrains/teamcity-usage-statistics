/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XmlUtil {
  @NotNull private static final Logger LOG = Logger.getLogger(XmlUtil.class);
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
        LOG.warn(e.getLocalizedMessage(), e);
      }
      finally {
        if (fos != null) {
          try {
              fos.close();
          }
          catch (final IOException e) {
            LOG.warn(e.getLocalizedMessage(), e);
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
        LOG.warn(e.getLocalizedMessage(), e);
      }
      catch (final IOException e) {
        LOG.warn(e.getLocalizedMessage(), e);
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
