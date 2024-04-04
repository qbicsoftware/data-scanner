package life.qbic.data.processing;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ConcurrentEventQueue {

  private final Queue<RegistrationRequestEvent> queue = new ConcurrentLinkedQueue<>();

  private static final int DEFAULT_CAPACITY = 10;
  private final int capacity;

  public ConcurrentEventQueue() {
    this(DEFAULT_CAPACITY);
  }

  public ConcurrentEventQueue(int capacity) {
    this.capacity = capacity;
  }

  public synchronized void add(RegistrationRequestEvent event) {
    while (queue.size() >= capacity) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    queue.add(event);
    notify();
  }

  public synchronized RegistrationRequestEvent poll() {
    while (queue.isEmpty()) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return queue.poll();
  }
}
