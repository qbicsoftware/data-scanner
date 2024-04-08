package life.qbic.data.processing;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>Provenance Information of Datasets</b>
 * <p>
 * Captures some dataset provenance metadata for pre-processing purposes.
 *
 * @since 1.0.0
 */
public class Provenance {

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
  @JsonProperty("id")
  public String id;
}
