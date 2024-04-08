package life.qbic.data.processing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Provenance Information of Datasets</b>
 * <p>
 * Captures some dataset provenance metadata for pre-processing purposes.
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties
public class Provenance {

  public static final String FILE_NAME = "provenance.json";

  /**
   * The path from where the dataset has been picked up originally.
   */
  @JsonProperty("origin")
  public String originPath;

  /**
   * The current location identifier of the dataset
   * <p>
   * Can be a file path when operating on file system level
   */
  @JsonProperty("history")
  public List<String> history;

  public void addToHistory(String event) {
    if (history == null) {
      history = new ArrayList<>();
    }
    history.add(event);
  }


}
