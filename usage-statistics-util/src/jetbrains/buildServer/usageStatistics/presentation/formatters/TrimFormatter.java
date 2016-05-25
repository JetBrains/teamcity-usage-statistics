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

package jetbrains.buildServer.usageStatistics.presentation.formatters;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Manuylov
 *         Date: 23.11.12
 */
public class TrimFormatter extends TypeBasedFormatter<String> {
  @NotNull private static final String ELLIPSIS = " <...>";
  private final int myMaxLength;

  public TrimFormatter(final int maxLength) {
    super(String.class);
    myMaxLength = maxLength;
  }

  @Override
  protected String doFormat(@NotNull final String statisticValue) {
    final String trimmedValue = statisticValue.trim();
    if (trimmedValue.length() <= myMaxLength) return trimmedValue;
    if (myMaxLength - ELLIPSIS.length() < 1) return trimmedValue.substring(0, myMaxLength).trim();
    return trimmedValue.substring(0, myMaxLength - ELLIPSIS.length()).trim() + ELLIPSIS;
  }
}
