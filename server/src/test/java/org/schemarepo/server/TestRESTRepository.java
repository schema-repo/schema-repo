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

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.schemarepo.InMemoryRepository;
import org.schemarepo.ValidatorFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.NotFoundException;
import org.schemarepo.json.GsonJsonUtil;

import javax.ws.rs.core.MediaType;

public class TestRESTRepository {
  RESTRepository repo;

  @Before
  public void setUp() {
    Properties properties = new Properties();
    properties.setProperty("key", "value");
    repo = new RESTRepository(new InMemoryRepository(new ValidatorFactory.Builder().build()),
		    new GsonJsonUtil(), properties);
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
    repo.subjectConfig("nothing");
  }

  @Test
  public void testCreateNullSubject() {
    Assert.assertEquals(400, repo.createSubject(null, null).getStatus());
  }

  @Test
  public void testGetConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new StringReader(repo.getConfig()));
    Assert.assertEquals("value", properties.getProperty("key"));
  }

}
