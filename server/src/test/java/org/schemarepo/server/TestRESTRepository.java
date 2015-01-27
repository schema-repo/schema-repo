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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.schemarepo.BaseRepository;
import org.schemarepo.InMemoryRepository;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.json.GsonJsonUtil;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestRESTRepository {

  BaseRepository backendRepo;
  RESTRepository repo;
  AuxiliaryRESTRepository auxRepo;

  @Before
  public void setUp() {
    Properties properties = new Properties();
    properties.setProperty("key", "value");
    backendRepo = new InMemoryRepository(new ValidatorFactory.Builder().build()) {
      @Override
      public void close() throws IOException {
        closed = true;
        super.close();
      }
    };
    repo = new MachineOrientedRESTRepository(backendRepo, new GsonJsonUtil());
    auxRepo = new AuxiliaryRESTRepository(backendRepo, properties);
  }

  @After
  public void tearDown() {
    repo = null;
  }

  @Test(expected=NotFoundException.class)
  public void testNonExistentSubjectList() throws Exception {
    repo.allSchemaEntries(MediaType.TEXT_PLAIN, "nothing");
  }

  @Test(expected=NotFoundException.class)
  public void testNonExistentSubjectGetConfig() throws Exception {
    repo.subjectConfig(null, "nothing");
  }

  @Test
  public void testCreateNullSubject() {
    assertEquals(400, repo.createSubject(null, null).getStatus());
  }

  @Test
  public void testGetConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new StringReader(auxRepo.getConfiguration(null, false).getEntity().toString()));
    assertEquals("value", properties.getProperty("key"));
  }

  @Test
  public void testGetStatus() throws Exception {
    Response response = auxRepo.getStatus();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    assertTrue(response.getEntity().toString().startsWith("OK"));
    backendRepo.close();
    response = auxRepo.getStatus();
    assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    assertFalse(response.getEntity().toString().startsWith("OK"));
  }

  @Test
  public void testInfluenceOfMediaTypeSuccess() {
    final String contentType = "Content-Type";
    repo.createSubject("dummy", new MultivaluedMapImpl());
    // null and all-inclusive (* or */*) mediaTypes result in the default configured renderer being used
    for (String mediaType: new String[] {null, "", "*/*", "text/plain", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"}) {
      Response response;
      try {
        response = repo.allSubjects(mediaType);
      } catch (WebApplicationException e) {
        response = e.getResponse();
      }
      assertEquals(Status.OK.getStatusCode(), response.getStatus());
      assertEquals(repo.getDefaultMediaType(), response.getMetadata().getFirst(contentType).toString());
    }

    Response response = repo.allSubjects("application/json");
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    assertEquals("application/json", response.getMetadata().getFirst(contentType).toString());
  }

  @Test
  public void testInfluenceOfMediaTypeFailure() {
    final String contentType = "Content-Type";
    Response response = null;
    try {
      repo.allSubjects("image/jpeg");
    } catch (WebApplicationException e) {
      response = e.getResponse();
    }
    assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
  }

}
