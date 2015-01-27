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

package org.schemarepo.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.schemarepo.InMemoryRepository;
import org.schemarepo.Repository;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.api.converter.AvroSchemaConverter;
import org.schemarepo.api.converter.ByteConverter;
import org.schemarepo.api.converter.Converter;
import org.schemarepo.api.converter.EnumConverter;
import org.schemarepo.api.converter.IdentityConverter;
import org.schemarepo.api.converter.IntegerConverter;
import org.schemarepo.api.converter.ShortConverter;

/**
 * Tests for the TypedSchemaRepository for ALL combinations of registered
 * repository implementations, ID, schema and subject types.
 */
public class TestTypedSchemaRepository {

  /**
   * This provider interface is necessary in order to recreate repository
   * instances for each test. Otherwise, their state could carry over across
   * tests.
   */
  public interface RepositoryProvider {
    public Repository createRepository();
  }

  private enum EnumSubjectExample {
    sub1, sub2
  }

  @Test
  public void testRepoCombinations(){
    List<RepositoryProvider> repositoryProviders = new ArrayList<RepositoryProvider>();
    List<Converter> idConverters = new ArrayList<Converter>();
    List<Converter> schemaConverters = new ArrayList<Converter>();
    List<Converter> subjectConverters = new ArrayList<Converter>();

    // Tested repositories
    repositoryProviders.add(new RepositoryProvider() {
      @Override
      public Repository createRepository() {
        return new InMemoryRepository(new ValidatorFactory.Builder().build());
      }
    });

    // TODO: Decide if we actually want to test other repo implementations here

    // Tested ID Converters
    idConverters.add(new IdentityConverter());
    idConverters.add(new ByteConverter());
    idConverters.add(new ShortConverter());
    idConverters.add(new IntegerConverter());

    // Tested Schema Converters
    schemaConverters.add(new IdentityConverter());
    schemaConverters.add(new AvroSchemaConverter(true));
    schemaConverters.add(new AvroSchemaConverter(false));

    // Tested Subject Converters
    subjectConverters.add(new IdentityConverter());
    subjectConverters.add(new EnumConverter(EnumSubjectExample.sub2));

    for (RepositoryProvider repoProvider: repositoryProviders) {
      for (Converter idConverter: idConverters) {
        for (Converter schemaConverter: schemaConverters) {
          for (Converter subjectConverter: subjectConverters) {
            testRegistration(repoProvider.createRepository(),
                    idConverter, schemaConverter, subjectConverter);

            // TODO: Add more tests
          }
        }
      }
    }
  }

  private <ID, SCHEMA, SUBJECT> void testRegistration(
          Repository innerRepo,
          Converter<ID> convertId,
          Converter<SCHEMA> convertSchema,
          Converter<SUBJECT> convertSubject) {

    String testName = "TypedSchemaRepository(" +
            innerRepo.getClass().getSimpleName() + ", " +
            convertId.getClass().getSimpleName() + ", " +
            convertSchema.getClass().getSimpleName() + ", " +
            convertSubject.getClass().getSimpleName() + "): ";

    try {
      TypedSchemaRepository<ID, SCHEMA, SUBJECT> repo =
              new TypedSchemaRepository<ID, SCHEMA, SUBJECT>
                      (innerRepo, convertId, convertSchema, convertSubject);

      SUBJECT subject1 = convertSubject.fromString("sub1");

      // TODO: Decouple Avro schema literals when we want to support other serialization
      SCHEMA subject1Schema1 = convertSchema.fromString("{\"type\":\"record\"," +
              "\"name\":\"subject1\",\"fields\":" +
              "[{\"name\":\"someId\",\"type\":\"long\"}," +
              "{\"name\":\"someString\",\"type\":[\"null\",\"string\"]}]}");
      SCHEMA subject1Schema2 = convertSchema.fromString("{\"type\":\"record\"," +
              "\"name\":\"subject1\",\"fields\":" +
              "[{\"name\":\"someId\",\"type\":\"long\"}," +
              "{\"name\":\"someString\",\"type\":[\"null\",\"string\"]}," +
              "{\"name\":\"someNewId\",\"type\":[\"null\",\"int\"]}]}");

      SCHEMA latestSchemaForSubject1 = repo.getLatestSchema(subject1);
      Assert.assertNull(testName + "Latest schema should be null before being registered.",
              latestSchemaForSubject1);

      // register 1st schema
      ID idForSubject1Schema1 = repo.registerSchema(subject1, subject1Schema1);

      // getLatestSchema
      latestSchemaForSubject1 = repo.getLatestSchema(subject1);
      Assert.assertEquals(testName + "getLatestSchema should be what we just registered.",
              subject1Schema1, latestSchemaForSubject1);

      // getSchema
      SCHEMA schema1ForSubject1ById = repo.getSchema(subject1, idForSubject1Schema1);
      Assert.assertEquals(testName + "getSchema by ID should be what we just registered.",
              subject1Schema1, schema1ForSubject1ById);

      // register 2nd schema
      ID idForSubject1Schema2 = repo.registerSchema(subject1, subject1Schema2);

      // getLatestSchema
      latestSchemaForSubject1 = repo.getLatestSchema(subject1);
      Assert.assertNotEquals(testName + "getLatestSchema should not still be the old schema.",
              subject1Schema1, latestSchemaForSubject1);
      Assert.assertEquals(testName + "getLatestSchema should be what we just registered.",
              subject1Schema2, latestSchemaForSubject1);

      // getSchema
      schema1ForSubject1ById = repo.getSchema(subject1, idForSubject1Schema1);
      Assert.assertEquals(testName + "getSchema for 1st ID should work.",
              subject1Schema1, schema1ForSubject1ById);

      SCHEMA schema2ForSubject1ById = repo.getSchema(subject1, idForSubject1Schema2);
      Assert.assertEquals(testName + "getSchema for 2nd ID should work.",
              subject1Schema2, schema2ForSubject1ById);

      // getSchemaId
      ID idForSubject1Schema1BySchema = repo.getSchemaId(subject1, subject1Schema1);
      ID idForSubject1Schema2BySchema = repo.getSchemaId(subject1, subject1Schema2);
      Assert.assertEquals(testName + "getSchemaId should return the same ID as during registration",
              idForSubject1Schema1, idForSubject1Schema1BySchema);
      Assert.assertEquals(testName + "getSchemaId should return the same ID as during registration",
              idForSubject1Schema2, idForSubject1Schema2BySchema);
      Assert.assertNotEquals(testName + "getSchemaId should not always return the same ID",
              idForSubject1Schema1BySchema, idForSubject1Schema2BySchema);

      // TODO: Add more registration-related test cases.

    } catch (SchemaValidationException e) {
      Assert.fail(testName + "A SchemaValidationException was thrown during testRegistration: " +
              e.getMessage());
    } finally {
      try {
        innerRepo.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
