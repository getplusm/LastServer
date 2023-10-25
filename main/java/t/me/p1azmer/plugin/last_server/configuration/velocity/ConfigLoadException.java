package t.me.p1azmer.plugin.last_server.configuration.velocity;

public class ConfigLoadException extends RuntimeException {

  public ConfigLoadException(Throwable cause) {
    this("An unexpected internal error was caught during (re-)loading the config.", cause);
  }

  public ConfigLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
