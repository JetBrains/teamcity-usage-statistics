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
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Provides TeamCity Server distribution type (.exe, .tar.gz, .war, etc) as stored in 'webapps/ROOT/WEB-INF/DistributionType.txt' file or 'unknown' if type cannot be determined
 */
public class ServerDistributionTypeProvider {
  private static final Logger LOG = Logger.getInstance(ServerDistributionTypeProvider.class.getName());
  public static final String UNKNOWN = "unknown";
  private static final String FILE_PATH = "/WEB-INF/DistributionType.txt";
  @NotNull private final ServletContext myContext;
  private String myDistributionType;

  public ServerDistributionTypeProvider(@NotNull final ServletContext context) {
    myContext = context;
  }

  @NotNull
  public String getDistributionType() {
    if (myDistributionType != null) return myDistributionType;
    myDistributionType = doGetDistributionType();
    return myDistributionType;
  }

  @NotNull
  private String doGetDistributionType() {
    try {
      final String path = myContext.getRealPath(FILE_PATH);
      if (path == null) {
        LOG.warn("Cannot determine TeamCity Distribution Type: cannot access web application filesystem");
        return UNKNOWN;
      }
      final File file = new File(path);
      if (!file.exists() || !file.isFile()) {
        LOG.warn("Cannot determine TeamCity Distribution Type: corresponding file not found");
        return UNKNOWN;
      }
      final String text = FileUtil.readText(file, "UTF-8");
      if (!StringUtil.isEmptyOrSpaces(text)) {
        return text;
      }
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Cannot determine TeamCity Distribution Type", e);
      return UNKNOWN;
    }
    return UNKNOWN;
  }
}
