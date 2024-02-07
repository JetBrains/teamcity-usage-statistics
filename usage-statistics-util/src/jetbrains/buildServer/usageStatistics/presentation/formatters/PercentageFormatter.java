
package jetbrains.buildServer.usageStatistics.presentation.formatters;

import org.jetbrains.annotations.NotNull;

public class PercentageFormatter extends TypeBasedFormatter<Integer> {
  private final int myTotal;

  public PercentageFormatter(final int total) {
    super(Integer.class);
    myTotal = total;
  }

  @NotNull
  @Override
  protected String doFormat(@NotNull final Integer count) {
    return myTotal == 0 ? String.valueOf(count) : count + " (" + (count * 100 / myTotal) + "%)";
  }
}