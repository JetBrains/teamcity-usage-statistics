/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.ArrayList;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IDEUsageStatisticsProviderTest {
  @DataProvider(name = "UserAgents")
  public static Iterator<String[]> UserAgents() {
    ArrayList<String[]> list = new ArrayList<>();
    list.add(new String[]{"", ""});
    list.add(new String[]{"", "     "});
    list.add(new String[]{"...", "  ...  "});
    list.add(new String[]{"10", "10"});

    list.add(new String[]{"IntelliJ IDEA 2016.3", "IntelliJ IDEA/2016.3 (IU-163.826; TeamCity Integration 10.0.SNAPSHOT)"});
    list.add(new String[]{"IntelliJ IDEA 2016.2", "IntelliJ IDEA/2016.2 (IU-162.1121.32; TeamCity Integration 10.0.41871)"});
    list.add(new String[]{"PhpStorm 2016.2", "PhpStorm/2016.2 (PS-162.1121.32; TeamCity Integration 10.0.41871)"});

    list.add(new String[]{"MS Visual Studio 10.0", "MS Visual Studio/10.0"});
    list.add(new String[]{"MS Visual Studio 12.0", "MS Visual Studio/12.0"});

    list.add(new String[]{"Eclipse based EPP RCP RAP Bundle 4.5.2", "Eclipse based EPP RCP/RAP Bundle 4.5.2 (linux; gtk; 10.0.0.41315)"});
    list.add(new String[]{"Eclipse based EPP RCP RAP Bundle 4.6.0", "Eclipse based EPP RCP/RAP Bundle 4.6.0 (linux; gtk; 10.0.0.20160714202417)"});

    list.add(new String[]{"Command Line Tool", "Command Line Tool"});

    return list.iterator();
  }

  @Test(dataProvider = "UserAgents")
  public void testPrepareUserAgent(String expected, String userAgent) throws Exception {
    Assert.assertEquals(IDEUsageStatisticsProvider.doPrepareUserAgent(userAgent), expected, "UA parsed incorrectly");
  }

}