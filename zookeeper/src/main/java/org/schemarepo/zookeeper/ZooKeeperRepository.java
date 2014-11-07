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

package org.schemarepo.zookeeper;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;

import org.schemarepo.Repository;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.server.ConfigKeys;

/**
 * This {@link org.schemarepo.Repository} implementation stores its state using Zookeeper.
 * <p/>
 * It requires the schema-repo.zookeeper.ensemble configuration property. This is
 * a comma-separated list of host:port addresses. Each address can also be suffixed
 * by a namespace, i.e.: zk.1:2181/schemas,zk.2:2181/schemas,zk.3:2181/schemas
 * <p/>
 * This Repository is meant to be highly available, meaning that multiple instances
 * can share the same Zookeeper ensemble and synchronize their state through it.
 */
public class ZooKeeperRepository implements Repository {

  private final ValidatorFactory validators;

  CuratorFramework curatorFramework;

  @Inject
  public ZooKeeperRepository(@Named(ConfigKeys.ZK_ENSEMBLE) String zkEnsemble,
                             @Named(ConfigKeys.ZK_PATH_PREFIX) String zkPathPrefix,
                             @Named(ConfigKeys.ZK_SESSION_TIMEOUT) Integer zkSessionTimeout,
                             @Named(ConfigKeys.ZK_CONNECTION_TIMEOUT) Integer zkConnectionTimeout,
                             @Named(ConfigKeys.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES) Integer curatorSleepTimeBetweenRetries,
                             @Named(ConfigKeys.ZK_CURATOR_NUMBER_OF_RETRIES) Integer curatorNumberOfRetries,
                             ValidatorFactory validators) {
    this.validators = validators;

    if (zkEnsemble == null || zkEnsemble.isEmpty()) {
      System.out.println("The '" + ConfigKeys.ZK_ENSEMBLE + "' config is missing. Exiting.");
      System.exit(1);
    }

    System.out.println("Starting ZookeeperRepository with the following parameters:\n" +
            ConfigKeys.ZK_ENSEMBLE + ": " + zkEnsemble + "\n" +
            ConfigKeys.ZK_PATH_PREFIX + ": " + zkPathPrefix + "\n" +
            ConfigKeys.ZK_SESSION_TIMEOUT + ": " + zkSessionTimeout + "\n" +
            ConfigKeys.ZK_CONNECTION_TIMEOUT + ": " + zkConnectionTimeout + "\n" +
            ConfigKeys.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES + ": " + curatorSleepTimeBetweenRetries + "\n" +
            ConfigKeys.ZK_CURATOR_NUMBER_OF_RETRIES + ": " + curatorNumberOfRetries);

    RetryPolicy retryPolicy = new RetryNTimes(
            curatorSleepTimeBetweenRetries,
            curatorNumberOfRetries);
    CuratorFrameworkFactory.Builder cffBuilder = CuratorFrameworkFactory.builder()
            .connectString(zkEnsemble)
            .sessionTimeoutMs(zkSessionTimeout)
            .connectionTimeoutMs(zkConnectionTimeout)
            .retryPolicy(retryPolicy);

    // This temporary CuratorFramework is not namespaced and is only used to ensure the
    // zkPathPrefix is properly initialized
    CuratorFramework tempCuratorFramework = cffBuilder.build();
    tempCuratorFramework.start();

    try {
      tempCuratorFramework.blockUntilConnected();
      tempCuratorFramework.create().forPath(zkPathPrefix);
      System.out.println("The ZK Path Prefix (" + zkPathPrefix + ") was created in ZK.");
    } catch (KeeperException.NodeExistsException e) {
      System.out.println("The ZK Path Prefix (" + zkPathPrefix + ") was found in ZK.");
    } catch (IllegalArgumentException e) {
      System.out.println("Got an IllegalArgumentException while attempting to create the " +
              "ZK Path Prefix (" + zkPathPrefix + "). Exiting.");
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      System.err.println("There was an unrecoverable exception during the ZooKeeperRepository startup. Exiting.");
      e.printStackTrace();
      System.exit(1);
    }

    // Once we're certain the zkPathPrefix is present, we initialize the CuratorFramework
    // we'll use for the rest of the ZK Repository's runtime.
    String zkPathPrefixWithoutLeadingSlash = zkPathPrefix.substring(1);
    curatorFramework = cffBuilder.namespace(zkPathPrefixWithoutLeadingSlash).build();
    curatorFramework.start();

    try {
      tempCuratorFramework.blockUntilConnected();
      System.out.println("ZooKeeperRepository startup finished!");
    } catch (Exception e) {
      System.err.println("There was an unrecoverable exception during the ZooKeeperRepository startup. Exiting.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Attempts to create a Subject with the given name and config.
   *
   * @param subjectName The name of the subject. Must not be null.
   * @param config      The subject configuration. May be null.
   * @return The newly created Subject, or an equivalent one if already created.
   * Does not return null.
   * @throws NullPointerException if subjectName is null
   */
  @Override
  public Subject register(String subjectName, SubjectConfig config) {
    return null;
  }

  /**
   * Returns the subject if it exists, null otherwise.
   *
   * @param subjectName the subject name
   * @return The subject if it exists, null otherwise.
   */
  @Override
  public Subject lookup(String subjectName) {
    return null;
  }

  /**
   * List all subjects. Does not return null.
   */
  @Override
  public Iterable<Subject> subjects() {
    return null;
  }
}
