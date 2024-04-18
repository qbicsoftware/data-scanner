package life.qbic.data.processing.config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class RoundRobinDraw<T> {

  private final ArrayList<T> items;
  private final int itemsAmount;
  private int currentIndex = 0;

  private RoundRobinDraw(Collection<T> items) {
    this.items = new ArrayList<>(items);
    this.itemsAmount = items.size();
  }

  public static <T> RoundRobinDraw<T> create(Collection<T> items) {
    if (items.isEmpty()) {
      throw new IllegalArgumentException("Empty collection");
    }
    return new RoundRobinDraw<>(items);
  }

  public synchronized T next() {
    if (currentIndex == itemsAmount) {
      currentIndex = 0;
    }
    T value = items.get(currentIndex);
    currentIndex++;
    return value;
  }

}
