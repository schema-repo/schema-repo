package org.schemarepo.server;

/**
 * Convenience class for importing the configuration keys for specific dependencies.
 */
public class ConfigKeys {
  // General Schema Repo configs
  private static final String PREFIX = "schema-repo.";
  public static final String REPO_CLASS = PREFIX + "class";
  public static final String REPO_CACHE = PREFIX + "cache";
  public static final String VALIDATOR_PREFIX = PREFIX + "validator.";

  // Jetty configs
  public static final String JETTY_HOST = PREFIX + "jetty.host";
  public static final String JETTY_PORT = PREFIX + "jetty.port";
  public static final String JETTY_PATH = PREFIX + "jetty.path";
  public static final String JETTY_HEADER_SIZE = PREFIX + "jetty.header.size";
  public static final String JETTY_BUFFER_SIZE = PREFIX + "jetty.buffer.size";

  // ZooKeeper configs
  public static final String ZK_ENSEMBLE = PREFIX + "zookeeper.ensemble";
  public static final String ZK_PATH_PREFIX = PREFIX + "zookeeper.path-prefix";
  public static final String ZK_SESSION_TIMEOUT = PREFIX + "zookeeper.session-timeout";
  public static final String ZK_CONNECTION_TIMEOUT = PREFIX + "zookeeper.connection-timeout";
  public static final String ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES = PREFIX + "zookeeper.curator.sleep-time-between-retries";
  public static final String ZK_CURATOR_NUMBER_OF_RETRIES = PREFIX + "zookeeper.curator.number-of-retries";
}
