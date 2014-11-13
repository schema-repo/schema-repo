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

import java.io.IOException;
import java.util.Date;

import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.BeforeClass;

import org.schemarepo.ValidatorFactory;
import org.schemarepo.config.Config;

public class TestZooKeeperRepository extends org.schemarepo.AbstractTestRepository<ZooKeeperRepository> {
  private static final String REPO_PATH = "/schema-repo-tests/" + new Date().toString().replace(' ', '_');
  private static final Integer numberOfServersInTestingCluster = 3;
  private static TestingCluster testingCluster;
  private static String testingClusterConnectionString;

  @BeforeClass
  public static void setup() {
    testingCluster = new TestingCluster(numberOfServersInTestingCluster);
    try {
      testingCluster.start();
      testingClusterConnectionString = testingCluster.getConnectString();

      // Potentially useful for manual debugging:
      // testingClusterConnectionString = "localhost:2181";
    } catch (Exception e) {
      System.err.println("An exception occurred while trying to start the ZK test cluster!");
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @After
  public void cleanUp() throws IOException {
    getRepo().close();
  }

  @Override
  protected ZooKeeperRepository createRepository() {
    return newRepo(REPO_PATH + "/" + Math.random());
  }

  private ZooKeeperRepository newRepo(String path) {
    return new ZooKeeperRepository(testingClusterConnectionString,
            path,
            Config.getIntDefault(Config.ZK_CONNECTION_TIMEOUT),
            Config.getIntDefault(Config.ZK_SESSION_TIMEOUT),
            Config.getIntDefault(Config.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES),
            Config.getIntDefault(Config.ZK_CURATOR_NUMBER_OF_RETRIES),
            new ValidatorFactory.Builder().build());
  }
}
