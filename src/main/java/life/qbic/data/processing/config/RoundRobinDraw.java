package life.qbic.data.processing.config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <b>Round Robin Draw</b>
 * <p>
 * Enables a thread-safe access of items in a given collection based on the round robin method.
 *
 * @since 1.0.0s
 */
public class RoundRobinDraw<T> {

  private final ArrayList<T> items;
  private final int itemsAmount;
  private int currentIndex = 0;

  private RoundRobinDraw(Collection<T> items) {
    this.items = new ArrayList<>(items);
    this.itemsAmount = items.size();
  }

  /**
   * Creates an instance of {@link RoundRobinDraw} based on the type {@link T} of the collection provided
   * @param items a collection of items the round robin method shall be applied.
   * @return an instance of this class
   * @throws IllegalArgumentException if an empty collection is provided or the collection is <code>null</code>
   * @since 1.0.0
   */
  public static <T> RoundRobinDraw<T> create(Collection<T> items) throws IllegalArgumentException {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Collection must not be null or empty");
    }
    return new RoundRobinDraw<>(items);
  }

  /**
   * Returns the next element {@link T} of a {@link RoundRobinDraw<T> } instance.
   * <p>
   * If the last item of the instance has been already been provided, it will start again from the
   * first item.
   *
   * @return an object of type {@link T}.
   * @since 1.0.0
   */
  public synchronized T next() {
    if (currentIndex == itemsAmount) {
      currentIndex = 0;
    }
    T value = items.get(currentIndex);
    currentIndex++;
    return value;
  }

}
