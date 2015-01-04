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

import static com.sun.jersey.api.client.ClientResponse.Status.CONFLICT;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static java.lang.String.format;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.schemarepo.BaseRepository;
import org.schemarepo.RepositoryUtil;
import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.config.Config;
import org.schemarepo.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * An Implementation of {@link org.schemarepo.Repository} that connects to a remote RESTRepository over HTTP.<br/>
 * <br/>
 * Typically, this is used in a client wrapped in a {@link org.schemarepo.CacheRepository} to limit network communication.<br/>
 * <br/>
 * Alternatively, this implementation can itself be what is used behind a RESTRepository in a RepositoryServer,
 * thus creating a caching proxy.
 *
 * <b>Note:</b>This implementation diverges from the original <a href='https://issues.apache.org/jira/browse/AVRO-1124'>AVRO-1124 issue</a>
 *
 * @see org.schemarepo.client.Avro1124RESTRepositoryClient
 */
public class RESTRepositoryClient extends BaseRepository implements RepositoryClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private WebResource webResource;
  private JsonUtil jsonUtil;
  private boolean returnNoneOnExceptions;

  @Inject
  public RESTRepositoryClient(@Named(Config.CLIENT_SERVER_URL) String url,
                              @Named(Config.JSON_UTIL_IMPLEMENTATION) JsonUtil jsonUtil,
                              @Named(Config.CLIENT_RETURN_NONE_ON_EXCEPTIONS) boolean returnNoneOnExceptions)
  {
    logger.info(format("Pointing to schema-repo server at %s", url));
    logger.info(format("Remote exceptions from GET requests will be %s",
            returnNoneOnExceptions ? "swallowed and an 'empty' value returned" : "propagated to the caller"));
    this.webResource = Client.create().resource(url);
    this.returnNoneOnExceptions = returnNoneOnExceptions;
    this.jsonUtil = jsonUtil;
  }

  @Override
  public Subject register(String subject, SubjectConfig config) {
    Form form = new Form();
    for(Map.Entry<String, String> entry : RepositoryUtil.safeConfig(config).asMap().entrySet()) {
      form.putSingle(entry.getKey(), entry.getValue());
    }

    String regSubjectName = webResource.path(subject)
            .accept(MediaType.TEXT_PLAIN)
            .type(MediaType.APPLICATION_FORM_URLENCODED)
            .put(String.class, form);

    return new RESTSubject(regSubjectName);
  }

  @Override
  public Subject lookup(String subject) {
    RepositoryUtil.validateSchemaOrSubject(subject);
    Subject s = null;
    try {//returns ok or exception if not found
      webResource.path(subject).get(String.class);
      s = new RESTSubject(subject);
    } catch (RuntimeException e) {
      handleException(e, format("Failed to lookup subject %s", subject), true);
    }
    return s;
  }

  @Override
  public Iterable<Subject> subjects() {
    ArrayList<Subject> subjectList = new ArrayList<Subject>();
    try {
      String subjects = webResource
              .accept(MediaType.APPLICATION_JSON)
              .get(String.class);
      for (String subjName : jsonUtil.subjectNamesFromJson(subjects)) {
        subjectList.add(new RESTSubject(subjName));
      }
    } catch (RuntimeException e) {
      handleException(e, "Failed to list all subjects", false);
    }
    return subjectList;
  }

  public String getStatus() {
    return webResource.path("status").get(String.class);
  }

  public Properties getConfiguration(final boolean includeDefaults) {
    final Properties properties = new Properties();
    try {
      final String propsData = webResource.path("config")
          .queryParam("includeDefaults", String.valueOf(includeDefaults)).get(String.class);
      properties.load(new StringReader(propsData));
    } catch (Exception e) {
      logger.error("Failed to fetch config", e);
    }
    return properties;
  }

  @Override
  protected Map<String, String> exposeConfiguration() {
    final Map<String, String> properties = new LinkedHashMap<String, String>(super.exposeConfiguration());
    properties.put(Config.CLIENT_SERVER_URL, webResource.getURI().toString());
    return properties;
  }

  private class RESTSubject extends Subject {

    private RESTSubject(String name) {
      super(name);
    }

    @Override
    public SubjectConfig getConfig() {
      String path = getName() + "/config" ;
      SubjectConfig config = null;
      try {
        String propString = webResource.path(path)
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        Properties props = new Properties();
        props.load(new StringReader(propString));
        config = RepositoryUtil.configFromProperties(props);
      } catch (RuntimeException e) {
        handleException(e, format("Failed to get config of subject %s", getName()), false);
      } catch (IOException e) {
        handleException(e, format("Failed to parse config data of subject %s", getName()), false);
      }
      return config;
    }

    @Override
    public SchemaEntry register(String schema) throws SchemaValidationException {
      RepositoryUtil.validateSchemaOrSubject(schema);
      String path = getName() + "/register";
      return handleRegisterRequest(path, schema, false);
    }

    @Override
    public SchemaEntry registerIfLatest(String schema, SchemaEntry latest) throws SchemaValidationException {
      RepositoryUtil.validateSchemaOrSubject(schema);
      String idStr = (latest == null) ? "" : latest.getId();
      String path = getName() + "/register_if_latest/" + idStr;
      return handleRegisterRequest(path, schema, true);
    }

    private SchemaEntry handleRegisterRequest(String path, String schema, boolean resourceNotFoundExpected) throws SchemaValidationException {
      SchemaEntry schemaEntry = null;
      try {
        String schemaId = webResource.path(path)
                .accept(MediaType.TEXT_PLAIN)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .put(String.class, schema);
        schemaEntry = new SchemaEntry(schemaId, schema);
      } catch (UniformInterfaceException e) {
        ClientResponse cr = e.getResponse();
        if (ClientResponse.Status.fromStatusCode(cr.getStatus()).equals(FORBIDDEN)) {
          throw new SchemaValidationException("Invalid schema: " + schema);
        } else {
          //any other status should return null
          handleException(e, format("Failed to register new schema for subject %s", getName()), resourceNotFoundExpected);
        }
      } catch (ClientHandlerException e) {
        handleException(e, format("Failed to register new schema for subject %s", getName()), resourceNotFoundExpected);
      }
      return schemaEntry;
    }

    @Override
    public SchemaEntry lookupBySchema(String schema) {
      RepositoryUtil.validateSchemaOrSubject(schema);
      String path = getName() + "/schema";
      SchemaEntry schemaEntry = null;
      try {
        String schemaId = webResource.path(path)
                .accept(MediaType.TEXT_PLAIN)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .post(String.class, schema);
        schemaEntry = new SchemaEntry(schemaId, schema);
      } catch (RuntimeException e) {
        handleException(e, format("Failed to locate schema %s in subject %s", schema, getName()), true);
      }
      return schemaEntry;
    }

    @Override
    public SchemaEntry lookupById(String schemaId) {
      RepositoryUtil.validateSchemaOrSubject(schemaId);
      String path = getName() + "/id/" + schemaId;
      SchemaEntry schemaEntry = null;
      try {
        String schema = webResource.path(path).get(String.class);
        schemaEntry = new SchemaEntry(schemaId, schema);
      } catch (RuntimeException e) {
        handleException(e, format("Failed to locate schema with ID %s in subject %s", schemaId, getName()), true);
      }
      return schemaEntry;
    }

    @Override
    public SchemaEntry latest() {
      String path = getName() + "/latest";
      SchemaEntry schemaEntry = null;
      try {
        String entryStr = webResource.path(path).get(String.class);
        schemaEntry = new SchemaEntry(entryStr);
      } catch (RuntimeException e) {
        handleException(e, format("Failed to locate latest schema in subject %s", getName()), true);
      }
      return schemaEntry;
    }

    @Override
    public Iterable<SchemaEntry> allEntries() {
      String path = getName() + "/all";
      Iterable<SchemaEntry> entries = Collections.emptyList();
      try {
        String entriesStr = webResource.path(path)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        entries = jsonUtil.schemasFromJson(entriesStr);
      } catch (RuntimeException e) {
        handleException(e, format("Failed to retrieve all schema entries in subject %s", getName()), false);
      }
      return entries;
    }

    @Override
    public boolean integralKeys() {
      boolean integral = false;
      try {
        String path = getName() + "/integral";
        integral = Boolean.parseBoolean(webResource.path(path).get(String.class));
      } catch (RuntimeException e) {
        handleException(e, format("Failed to determine if keys are integral in subject %s", getName()), false);
      }
      return integral;
    }

  }


  private void handleException(Exception ex, String msg, boolean resourceNotFoundExpected) {
    final ClientResponse.Status status = ex instanceof UniformInterfaceException ?
        ((UniformInterfaceException)ex).getResponse().getClientResponseStatus() : null;
    if (status == NOT_FOUND && resourceNotFoundExpected || status == CONFLICT) {
      logger.debug(msg, ex);
    } else if (returnNoneOnExceptions) {
      if (status != null && status.getFamily() == Response.Status.Family.CLIENT_ERROR) {
        logger.info(msg, ex);
      } else {
        logger.error(msg, ex);
      }
    } else {
      throw ex instanceof RuntimeException ? (RuntimeException)ex : new RuntimeException(msg, ex);
    }
  }

}
