package es.upm.woa.group3.util;

import static es.upm.woa.group3.util.GameSettings.GROUP3_NAME;
import static java.util.Objects.requireNonNull;

public class Logger {
  private String aClassName;

  private Logger(String aClassName) {
    this.aClassName = requireNonNull(aClassName);
  }

  public static Logger getLogger(String agentName) {
    return new Logger(requireNonNull(agentName));
  }

  public void log(String message) {
    System.out.println(
        String.format("[%s]: %s: %s", GROUP3_NAME, this.aClassName.toUpperCase(), message));
  }
}
