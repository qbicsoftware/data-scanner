package life.qbic.data.processing;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Error Summary</b>
 *
 * <p>Provides the data submitter with some helpful contextual information about a
 * registration failure</p>
 *
 * @since 1.0.0
 */
public record ErrorSummary(String taskId, String affectedDataset, String reason, String description,
                           Map<String, String> contextProperties) {

  public static ErrorSummary create(String taskId, String affectedDataset, String reason,
      String description, Map<String, String> contextProperties) {
    return new ErrorSummary(taskId, affectedDataset, reason, description,
        new HashMap<>(contextProperties));
  }

  public static ErrorSummary createSimple(String taskId, String affectedDataset, String reason,
      String description) {
    return new ErrorSummary(taskId, affectedDataset, reason, description, new HashMap<>());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Task ID: ").append(taskId).append("\n");
    sb.append("Affected Dataset: ").append(affectedDataset).append("\n");
    sb.append("Reason: ").append(reason).append("\n");
    sb.append("Description: ").append(description).append("\n");
    for (Map.Entry<String, String> entry : contextProperties.entrySet()) {
      sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }
}
