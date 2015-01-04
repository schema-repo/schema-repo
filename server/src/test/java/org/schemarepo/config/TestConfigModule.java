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

import org.junit.Assert;
import org.junit.Test;
import org.schemarepo.InMemoryRepository;
import org.schemarepo.Repository;
import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.Validator;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestConfigModule {

  @Test
  public void testConfig() {
    Properties props = new Properties();
    props.setProperty(Config.REPO_CLASS, InMemoryRepository.class.getName());
    props.put(Config.VALIDATOR_PREFIX + "rejectAll", Reject.class.getName());
    ConfigModule module = new ConfigModule(props);
    Injector injector = Guice.createInjector(module);
    Repository repo = injector.getInstance(Repository.class);
    Subject rejects = repo.register("rejects", new SubjectConfig.Builder()
        .addValidator("rejectAll").build());
    boolean threw = false;
    try {
      rejects.register("stuff");
    } catch (SchemaValidationException se) {
      threw = true;
    }
    Assert.assertTrue(threw);
  }

  @Test
  public void testPrintDefaults() {
    ConfigModule.printDefaults(System.out);
  }

  public static class Reject implements Validator {
    @Override
    public void validate(String schemaToValidate,
        Iterable<SchemaEntry> schemasInOrder) throws SchemaValidationException {
      throw new SchemaValidationException("no");
    }
  }

}
