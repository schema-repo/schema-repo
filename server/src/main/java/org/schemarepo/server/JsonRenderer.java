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

import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.schemarepo.SchemaEntry;
import org.schemarepo.Subject;
import org.schemarepo.json.JsonUtil;

/**
 * Renders as JSON. The actual work is delegated to utility class {@link org.schemarepo.RepositoryUtil}
 */
public class JsonRenderer implements Renderer {

  private final JsonUtil jsonUtil;

  public JsonRenderer(JsonUtil jsonUtil) {
    if (jsonUtil == null) {
      throw new IllegalArgumentException("jsonUtil required");
    }
    this.jsonUtil = jsonUtil;
  }

  @Override
  public String getMediaType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public String renderSubjects(Iterable<Subject> subjects) {
    return jsonUtil.subjectsToJson(subjects);
  }

  @Override
  public String renderSchemas(Iterable<SchemaEntry> schemaEntries) {
    return jsonUtil.schemasToJson(schemaEntries);
  }

  @Override
  public String renderSchemaEntry(SchemaEntry schemaEntry, boolean requestForLatest) {
    return notAcceptable("renderSchemaEntry");
  }

  @Override
  public String renderProperties(Properties props, String comment) {
    return notAcceptable("renderProperties");
  }

  private String notAcceptable(String api) {
    throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
        .entity(String.format("%s API does not support %s media type", api, getMediaType())).build());
  }

}
