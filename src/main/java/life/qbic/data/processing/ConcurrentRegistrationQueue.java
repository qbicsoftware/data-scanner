package life.qbic.data.processing;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import life.qbic.data.processing.registration.RegistrationRequest;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class ConcurrentRegistrationQueue {

  private final Queue<RegistrationRequest> queue = new LinkedBlockingQueue<>();

  private static final int DEFAULT_CAPACITY = 10;
  private final int capacity;

  public ConcurrentRegistrationQueue() {
    this(DEFAULT_CAPACITY);
  }

  public ConcurrentRegistrationQueue(int capacity) {
    this.capacity = capacity;
  }

  public synchronized void add(RegistrationRequest request) {
    while (queue.size() >= capacity) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    queue.add(request);
    notify();
  }

  public synchronized RegistrationRequest poll() {
    while (queue.isEmpty()) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    var request = queue.poll();
    if (queue.size() == capacity - 1) {
      notifyAll();
    }
    return request;
  }

  public synchronized boolean hasItems() {
    return !queue.isEmpty();
  }

  public int items() {
    return queue.size();
  }
}
