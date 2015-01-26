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

package org.schemarepo.server;

import java.util.Arrays;

import javax.ws.rs.Path;

import org.schemarepo.Repository;
import org.schemarepo.json.JsonUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Subclass of {@link org.schemarepo.server.RESTRepository} which supports machine-oriented rendering
 * (plain text and JSON).
 */
@Singleton
@Path("/schema-repo")
public class MachineOrientedRESTRepository extends RESTRepository {

  /**
   * All parameters will be injected by Guice framework.
   * @param repo the backend repository
   * @param jsonUtil implementation of JSON utils
   */
  @Inject
  public MachineOrientedRESTRepository(Repository repo, JsonUtil jsonUtil) {
    super(repo, Arrays.asList(new PlainTextRenderer(), new JsonRenderer(jsonUtil)));
  }

}
