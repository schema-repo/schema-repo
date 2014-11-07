package org.schemarepo.config;

import java.io.PrintStream;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import org.schemarepo.CacheRepository;
import org.schemarepo.InMemoryCache;
import org.schemarepo.Repository;
import org.schemarepo.RepositoryCache;
import org.schemarepo.Validator;
import org.schemarepo.ValidatorFactory;

/**
 * A {@link Module} for configuration based on a set of {@link Properties}
 * <br/>
 * Binds every property value in the properties provided to the property name
 * in Guice, making them available with the {@link Named} annotation.  Guice
 * will automatically convert these to constant values, such as Integers,
 * Strings, or Class constants.
 * <br/>
 * Keys starting with "validator." bind {@link Validator} classes
 * in a {@link ValidatorFactory}, where the name is the remainder of the key
 * following "schema-repo.validator.".  For example, a property
 * "schema-repo.validator.backwards_compatible=com.foo.BackwardsCompatible"
 * will set a validator named "backwards_compatible" to an instance of the
 * class com.foo.BackwardsCompatible.
 */
public class ConfigModule implements Module {

  private static final Properties DEFAULTS = new Properties();
  static {
    DEFAULTS.setProperty(ConfigKeys.REPO_CACHE, InMemoryCache.class.getName());

    // Jetty defaults
    DEFAULTS.setProperty(ConfigKeys.JETTY_HOST, "");
    DEFAULTS.setProperty(ConfigKeys.JETTY_PORT, "2876"); // 'AVRO' on a t-9 keypad
    DEFAULTS.setProperty(ConfigKeys.JETTY_PATH, "/schema-repo");
    DEFAULTS.setProperty(ConfigKeys.JETTY_HEADER_SIZE, "16384");
    DEFAULTS.setProperty(ConfigKeys.JETTY_BUFFER_SIZE, "16384");

    // Zookeeper backend defaults
    DEFAULTS.setProperty(ConfigKeys.ZK_ENSEMBLE, "");
    DEFAULTS.setProperty(ConfigKeys.ZK_PATH_PREFIX, "/schema-repo");
    DEFAULTS.setProperty(ConfigKeys.ZK_SESSION_TIMEOUT, "5000");
    DEFAULTS.setProperty(ConfigKeys.ZK_CONNECTION_TIMEOUT, "2000");
    DEFAULTS.setProperty(ConfigKeys.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES, "2000");
    DEFAULTS.setProperty(ConfigKeys.ZK_CURATOR_NUMBER_OF_RETRIES, "10");
  }

  public static void printDefaults(PrintStream writer) {
    writer.println(DEFAULTS);
  }

  private final Properties props;

  public ConfigModule(Properties props) {
    Properties copy = new Properties(DEFAULTS);
    copy.putAll(props);
    this.props = copy;
  }

  @Override
  public void configure(Binder binder) {
    Names.bindProperties(binder, props);
  }

  @Provides
  @Singleton
  Repository provideRepository(Injector injector,
      @Named(ConfigKeys.REPO_CLASS) Class<Repository> repoClass,
      @Named(ConfigKeys.REPO_CACHE) Class<RepositoryCache> cacheClass) {
    Repository repo = injector.getInstance(repoClass);
    RepositoryCache cache = injector.getInstance(cacheClass);
    return new CacheRepository(repo, cache);
  }

  @Provides
  @Singleton
  ValidatorFactory provideValidatorFactory(Injector injector) {
    ValidatorFactory.Builder builder = new ValidatorFactory.Builder();
    for(String prop : props.stringPropertyNames()) {
      if (prop.startsWith(ConfigKeys.VALIDATOR_PREFIX)) {
        String validatorName = prop.substring(ConfigKeys.VALIDATOR_PREFIX.length());
        Class<Validator> validatorClass = injector.getInstance(
            Key.<Class<Validator>>get(
                new TypeLiteral<Class<Validator>>(){}, Names.named(prop)));
        builder.setValidator(validatorName, injector.getInstance(validatorClass));
      }
    }
    return builder.build();
  }
}
