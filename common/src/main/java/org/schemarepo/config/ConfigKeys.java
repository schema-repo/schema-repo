package org.schemarepo.config;

/**
 * Convenience class for importing the configuration keys for specific dependencies.
 */
public class ConfigKeys {
  // General Schema Repo configs
  private static final String GLOBAL_PREFIX = "schema-repo.";
  public static final String REPO_CLASS = GLOBAL_PREFIX + "class";
  public static final String REPO_CACHE = GLOBAL_PREFIX + "cache";
  public static final String VALIDATOR_PREFIX = GLOBAL_PREFIX + "validator.";

  // Jetty configs
  private static final String JETTY_PREFIX = GLOBAL_PREFIX + "jetty.";
  public static final String JETTY_HOST = JETTY_PREFIX + "host";
  public static final String JETTY_PORT = JETTY_PREFIX + "port";
  public static final String JETTY_PATH = JETTY_PREFIX + "path";
  public static final String JETTY_HEADER_SIZE = JETTY_PREFIX + "header.size";
  public static final String JETTY_BUFFER_SIZE = JETTY_PREFIX + "buffer.size";

  // Local file system backend configs
  private static final String LOCAL_FILE_SYSTEM_PREFIX = GLOBAL_PREFIX + "local-file-system.";
  public static final String LOCAL_FILE_SYSTEM_PATH = LOCAL_FILE_SYSTEM_PREFIX + "path";

  // ZooKeeper backend configs
  private static final String ZK_PREFIX = GLOBAL_PREFIX + "zookeeper.";
  public static final String ZK_ENSEMBLE = ZK_PREFIX + "ensemble";
  public static final String ZK_PATH_PREFIX = ZK_PREFIX + "path-prefix";
  public static final String ZK_SESSION_TIMEOUT = ZK_PREFIX + "session-timeout";
  public static final String ZK_CONNECTION_TIMEOUT = ZK_PREFIX + "connection-timeout";
  public static final String ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES = ZK_PREFIX + "curator.sleep-time-between-retries";
  public static final String ZK_CURATOR_NUMBER_OF_RETRIES = ZK_PREFIX + "curator.number-of-retries";
}
