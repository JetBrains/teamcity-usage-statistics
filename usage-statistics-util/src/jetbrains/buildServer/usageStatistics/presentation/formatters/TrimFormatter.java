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
