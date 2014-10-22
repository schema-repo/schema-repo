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

import org.schemarepo.Repository;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.ValidatorFactory;
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
//import org.apache.zookeeper.data.ACL;

import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

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
  private static final String ROOT_ZK_PATH = "/schema-repo";
  private static final Integer ZK_SESSION_TIMEOUT = 5000;

  ZooKeeper zk;
  Watcher zkWatcher;

  @Inject
  public ZooKeeperRepository(@Named("schema-repo.zookeeper.ensemble") String zkEnsemble,
                             ValidatorFactory validators) {
    zkWatcher = new ZooKeeperRepositoryWatcher();

    try {
      zk = new ZooKeeper(zkEnsemble, ZK_SESSION_TIMEOUT, zkWatcher);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

//    List<ACL> aclList = new ArrayList<ACL>();
//    aclList.add(new ACL());
//
//    try {
//      zk.create(ROOT_ZK_PATH, null, aclList, CreateMode.PERSISTENT);
//      System.out.println("The root znode for the schema repo was not found. " +
//              "It has been created for the first time.");
//    } catch (KeeperException.NodeExistsException e) {
//      System.out.println("The znode for the schema repo was found.");
//    } catch (KeeperException e) {
//      System.err.println("A KeeperException occurred while trying to create the root znode!");
//      e.printStackTrace();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
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
