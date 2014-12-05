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

package org.schemarepo.client;

import java.util.Properties;

import org.schemarepo.AbstractTestRepository;
import org.schemarepo.InMemoryRepository;
import org.schemarepo.config.Config;
import org.schemarepo.server.RepositoryServer;

public abstract class AbstractTestRepositoryClient<R extends RepositoryClient> extends AbstractTestRepository<R> {

  protected RepositoryServer server;

  @Override
  protected R createRepository() throws Exception {
    Properties props = new Properties();
    props.put(Config.REPO_CLASS, InMemoryRepository.class.getName());
    props.put(Config.JETTY_HOST, "localhost");
    props.put(Config.JETTY_PORT, "8123");
    props.put(Config.JETTY_GRACEFUL_SHUTDOWN, "100");
    server = new RepositoryServer(props);
    server.start();
    return createClient("http://localhost:8123/schema-repo/");
  }

  protected abstract R createClient(String repoUrl);

  @Override
  public void tearDownRepository() throws Exception {
    server.stop();
    super.tearDownRepository();
  }

}
