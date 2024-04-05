package life.qbic.data.processing;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b><class short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public class Provenance {

  @JsonProperty("origin")
  public String originPath;

  @JsonProperty("id")
  public String id;
}
