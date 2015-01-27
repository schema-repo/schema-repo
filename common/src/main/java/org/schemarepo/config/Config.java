/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.schemarepo.config;

import java.util.Properties;

import org.schemarepo.InMemoryCache;
import org.schemarepo.json.GsonJsonUtil;

/**
 * Class containing the configuration keys and default values.
 */
public class Config {
  // General Schema Repo configs
  private static final String GLOBAL_PREFIX = "schema-repo.";
  public static final String REPO_CLASS = GLOBAL_PREFIX + "class";
  public static final String REPO_CACHE = GLOBAL_PREFIX + "cache";
  public static final String VALIDATOR_PREFIX = GLOBAL_PREFIX + "validator.";

  // Jetty configs
  private static final String JETTY_PREFIX = GLOBAL_PREFIX + "jetty.";
  public static final String JETTY_HOST = JETTY_PREFIX + "host";
  public static final String JETTY_PORT = JETTY_PREFIX + "port";
  public static final String JETTY_HEADER_SIZE = JETTY_PREFIX + "header.size";
  public static final String JETTY_BUFFER_SIZE = JETTY_PREFIX + "buffer.size";
  public static final String JETTY_STOP_AT_SHUTDOWN = JETTY_PREFIX + "stop-at-shutdown";
  public static final String JETTY_GRACEFUL_SHUTDOWN = JETTY_PREFIX + "graceful-shutdown";

  // Logging config
  private static final String LOGGING_PREFIX = GLOBAL_PREFIX + "logging.";
  public static final String LOGGING_ROUTE_JUL_TO_SLF4J = LOGGING_PREFIX + "route-jul-to-slf4j";

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

  // REST client config
  private static final String CLIENT_PREFIX = GLOBAL_PREFIX + "rest-client.";
  public static final String CLIENT_SERVER_URL = CLIENT_PREFIX + "server-url";
  public static final String CLIENT_RETURN_NONE_ON_EXCEPTIONS = CLIENT_PREFIX + "return-none-on-exceptions";

  // JSON parser config
  private static final String JSON_PREFIX = GLOBAL_PREFIX + "json.";
  public static final String JSON_UTIL_IMPLEMENTATION = JSON_PREFIX + "util-implementation";

  // Default values for the above

  public static final Properties DEFAULTS = new Properties();
  static {
    // General defaults
    DEFAULTS.setProperty(REPO_CACHE, InMemoryCache.class.getName());

    // Jetty defaults
    DEFAULTS.setProperty(JETTY_HOST, "");
    DEFAULTS.setProperty(JETTY_PORT, "2876"); // 'AVRO' on a t-9 keypad
    DEFAULTS.setProperty(JETTY_HEADER_SIZE, "16384");
    DEFAULTS.setProperty(JETTY_BUFFER_SIZE, "16384");
    DEFAULTS.setProperty(JETTY_STOP_AT_SHUTDOWN, "true");
    DEFAULTS.setProperty(JETTY_GRACEFUL_SHUTDOWN, "3000");

    // Logging defaults
    DEFAULTS.setProperty(LOGGING_ROUTE_JUL_TO_SLF4J, "true");

    // Zookeeper backend defaults
    DEFAULTS.setProperty(ZK_ENSEMBLE, "");
    DEFAULTS.setProperty(ZK_PATH_PREFIX, "/schema-repo");
    DEFAULTS.setProperty(ZK_SESSION_TIMEOUT, "5000");
    DEFAULTS.setProperty(ZK_CONNECTION_TIMEOUT, "2000");
    DEFAULTS.setProperty(ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES, "2000");
    DEFAULTS.setProperty(ZK_CURATOR_NUMBER_OF_RETRIES, "10");

    // Client defaults
    DEFAULTS.setProperty(CLIENT_RETURN_NONE_ON_EXCEPTIONS, "true");

    // JSON defaults
    DEFAULTS.setProperty(JSON_UTIL_IMPLEMENTATION, GsonJsonUtil.class.getName());
  }

  public static String getDefault(String propertyName) {
    return DEFAULTS.getProperty(propertyName);
  }

  public static Integer getIntDefault(String propertyName) {
    return Integer.parseInt(getDefault(propertyName));
  }
}
