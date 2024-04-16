package life.qbic.data.processing;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import life.qbic.data.processing.registration.RegistrationRequest;
import org.apache.logging.log4j.Logger;

/**
 * <b>Concurrent Registration Queue</b>
 * <p>
 * Simple FIFO queue, that allows for backpressure.
 *
 * @since 1.0.0
 */
public class ConcurrentRegistrationQueue {

  private static final int DEFAULT_CAPACITY = 10;
  private final Queue<RegistrationRequest> queue = new LinkedBlockingQueue<>();
  private final int capacity;
  private static final Logger log = getLogger(ConcurrentRegistrationQueue.class);


  public ConcurrentRegistrationQueue() {
    this(DEFAULT_CAPACITY);
  }

  public ConcurrentRegistrationQueue(int capacity) {
    this.capacity = capacity;
  }

  /**
   * Adds a new {@link RegistrationRequest} to the registration queue.
   * <p>
   * If the queue has reached its maximal capacity, the calling thread is put into the wait state,
   * until the queue's load is reduced below its configured maximal capacity.
   *
   * @param request the request to add to the registration queue
   * @since 1.0.0
   */
  public synchronized void add(RegistrationRequest request) {
    while (queue.size() >= capacity) {
      try {
        wait();
      } catch (InterruptedException e) {
        log.error("Interrupted while waiting for registration request", e);
        Thread.currentThread().interrupt();
      }
    }
    queue.add(request);
    notifyAll();
  }

  /**
   * Requests the next {@link RegistrationRequest} in the queue.
   * <p>
   * If the queue is empty, the calling thread is put into the wait state (via
   * {@link Object#wait()}) until it gets notified again when a new task is available in the queue.
   *
   * @return the next registration request available.
   * @since 1.0.0
   */
  public synchronized RegistrationRequest poll() {
    while (queue.isEmpty()) {
      try {
        wait();
      } catch (InterruptedException e) {
        log.error("Interrupted while waiting for registration request", e);
        Thread.currentThread().interrupt();
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
