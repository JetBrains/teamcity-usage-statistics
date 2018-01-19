/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.presentation.formatters;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TypeBasedFormatter<T> implements UsageStatisticsFormatter {
  @NotNull private final Class<T> myType;

  public TypeBasedFormatter(@NotNull final Class<T> type) {
    myType = type;
  }

  @NotNull
  public String format(@Nullable final Object statisticValue) {
    if (statisticValue == null || !myType.isInstance(statisticValue)) return StringUtil.NA;
    //noinspection unchecked
    return doFormat((T)statisticValue);
  }

  protected abstract String doFormat(@NotNull T statisticValue);
}
