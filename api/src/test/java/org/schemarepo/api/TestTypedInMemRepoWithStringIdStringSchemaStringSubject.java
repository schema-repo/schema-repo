package org.schemarepo.api;

import org.schemarepo.InMemoryRepository;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.api.converter.Converter;
import org.schemarepo.api.converter.IdentityConverter;

public class TestTypedInMemRepoWithStringIdStringSchemaStringSubject
        extends TestTypedSchemaRepository<InMemoryRepository, String, String, String>{
  @Override
  protected InMemoryRepository getInnerRepo() {
    return new InMemoryRepository(new ValidatorFactory.Builder().build());
  }

  @Override
  protected Converter<String> getIdConverter() {
    return new IdentityConverter();
  }

  @Override
  protected Converter<String> getSchemaConverter() {
    return new IdentityConverter();
  }

  @Override
  protected Converter<String> getSubjectConverter() {
    return new IdentityConverter();
  }

}
