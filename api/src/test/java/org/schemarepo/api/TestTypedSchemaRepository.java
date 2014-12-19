package org.schemarepo.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.schemarepo.Repository;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.api.converter.Converter;

/**
 * Tests for the TypedSchemaRepository
 */
public abstract class TestTypedSchemaRepository<
        INNER_REPO extends Repository, ID, SCHEMA, SUBJECT> {

  private TypedSchemaRepository<INNER_REPO, ID, SCHEMA, SUBJECT> repo;
  private Converter<ID> convertId = getIdConverter();
  private Converter<SCHEMA> convertSchema = getSchemaConverter();
  private Converter<SUBJECT> convertSubject = getSubjectConverter();

  protected abstract INNER_REPO getInnerRepo();
  protected abstract Converter<ID> getIdConverter();
  protected abstract Converter<SCHEMA> getSchemaConverter();
  protected abstract Converter<SUBJECT> getSubjectConverter();
  protected TypedSchemaRepository getRepo() {
    return new TypedSchemaRepository(
            getInnerRepo(),
            getIdConverter(),
            getSchemaConverter(),
            getSubjectConverter());
  }

  @Before
  public void setUpRepo() {
    repo = getRepo();
  }

  private SUBJECT subject1 = convertSubject.fromString("sub1");
  private SCHEMA subject1Schema1 = convertSchema.fromString("schema1");

  @Test
  public void testRegistration() throws SchemaValidationException {
    // Latest schema should be null before being registered
    SCHEMA latestSchemaForSubject1 = repo.getLatestSchema(subject1);
    Assert.assertNull(latestSchemaForSubject1);

    // Registration
    ID idForSubject1Schema1 = repo.registerSchema(subject1, subject1Schema1);

    // Latest schema should return what we just registered
    latestSchemaForSubject1 = repo.getLatestSchema(subject1);
    Assert.assertNotNull(latestSchemaForSubject1);
    Assert.assertEquals(subject1Schema1, latestSchemaForSubject1);

    // getSchema by ID should return what we just registered
    SCHEMA schema1ForSubject1ById = repo.getSchema(subject1, idForSubject1Schema1);
    Assert.assertNotNull(latestSchemaForSubject1);
    Assert.assertEquals(subject1Schema1, latestSchemaForSubject1);
  }

}
