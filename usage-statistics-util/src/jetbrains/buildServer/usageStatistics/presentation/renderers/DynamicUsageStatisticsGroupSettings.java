/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.presentation.renderers;

import com.intellij.openapi.util.Key;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface DynamicUsageStatisticsGroupSettings {
  @NotNull @NonNls public static final Key<List<String>> PERIODS = Key.create("dynamic.periods");
  @NotNull @NonNls public static final Key<String> DEFAULT_VALUE = Key.create("dynamic.default.value");
  @NotNull @NonNls public static final Key<Boolean> SORT = Key.create("dynamic.sort");
}
