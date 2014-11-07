package org.schemarepo.config;

import java.util.Properties;

import org.schemarepo.InMemoryRepository;
import org.schemarepo.Repository;
import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.Validator;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestConfigModule {

  @Test
  public void testConfig() {
    Properties props = new Properties();
    props.setProperty(ConfigKeys.REPO_CLASS, InMemoryRepository.class.getName());
    props.put(ConfigKeys.VALIDATOR_PREFIX + "rejectAll", Reject.class.getName());
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
