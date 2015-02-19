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

import java.io.PrintStream;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.multibindings.MapBinder;
import org.schemarepo.CacheRepository;
import org.schemarepo.Repository;
import org.schemarepo.RepositoryCache;
import org.schemarepo.RepositoryUtil;
import org.schemarepo.Validator;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.json.JsonUtil;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import org.schemarepo.validation.AlwaysFailValidationStrategy;
import org.schemarepo.validation.CanBeReadValidationStrategy;
import org.schemarepo.validation.MutualReadValidationStrategy;
import org.schemarepo.validation.ValidationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static void printDefaults(PrintStream writer) {
    writer.println(Config.DEFAULTS);
  }

  private final Properties props;

  public ConfigModule(Properties props) {
    Properties copy = new Properties(Config.DEFAULTS);
    copy.putAll(props);
    this.props = copy;
  }

  @Override
  public void configure(Binder binder) {
    Names.bindProperties(binder, props);
    registerValidationStrategies(binder);
    bindSubModules(binder);
  }

  /**
   * Registers the included ValidationStrategy implementations with the map of all ValidationStrategies
   * @param binder
   *    The binder to register with.
   */
  private void registerValidationStrategies(Binder binder) {
    MapBinder<String,ValidationStrategy> strategyMap = MapBinder.newMapBinder(binder,String.class,ValidationStrategy.class);
    strategyMap.addBinding(CanBeReadValidationStrategy.class.getCanonicalName()).to(CanBeReadValidationStrategy.class);
    strategyMap.addBinding(MutualReadValidationStrategy.class.getCanonicalName()).to(MutualReadValidationStrategy.class);
    strategyMap.addBinding(AlwaysFailValidationStrategy.class.getCanonicalName()).to(AlwaysFailValidationStrategy.class);
  }

  /**
   * Binds all configured sub-modules from other plugins. Modules are configured using the prefix ("schema-repo.module.*")
   * @param binder
   *    The binder to register with.
   */
  private void bindSubModules(Binder binder) {
    for(String prop : props.stringPropertyNames()) {
      if (prop.startsWith(Config.MODULE_PREFIX)) {
        Class<Module> moduleClass;
        Module configModule = null;
        try {
          //noinspection unchecked
          moduleClass = (Class<Module>)Class.forName(props.getProperty(prop));
          configModule = moduleClass.getConstructor().newInstance();
        }
        catch (Exception e) {
          logger.error("Exception during binding of sub module " + props.getProperty(prop), e);
        }

        if (configModule != null)
          binder.install(configModule);
      }
    }
  }

  @Provides
  @Singleton
  Repository provideRepository(Injector injector,
      @Named(Config.REPO_CLASS) Class<Repository> repoClass,
      @Named(Config.REPO_CACHE) Class<RepositoryCache> cacheClass) {
    Repository repo = injector.getInstance(repoClass);
    RepositoryCache cache = injector.getInstance(cacheClass);
    return new CacheRepository(repo, cache);
  }

  @Provides
  @Singleton
  ValidatorFactory provideValidatorFactory(Injector injector, @Named(Config.DEFAULT_SUBJECT_VALIDATORS) String defaultSubjectValidators) {
    ValidatorFactory.Builder builder = new ValidatorFactory.Builder();
    for(String prop : props.stringPropertyNames()) {
      if (prop.startsWith(Config.VALIDATOR_PREFIX)) {
        String validatorName = prop.substring(Config.VALIDATOR_PREFIX.length());
        Class<Validator> validatorClass = injector.getInstance(
            Key.<Class<Validator>>get(
                new TypeLiteral<Class<Validator>>(){}, Names.named(prop)));
        builder.setValidator(validatorName, injector.getInstance(validatorClass));
      }
    }

    // assign the default subject validators
    builder.setDefaultValidators(RepositoryUtil.commaSplit(defaultSubjectValidators));
    return builder.build();
  }

  @Provides
  @Singleton
  JsonUtil provideJsonUtil(Injector injector,
      @Named(Config.JSON_UTIL_IMPLEMENTATION) Class<JsonUtil> jsonUtilClass) {
    return injector.getInstance(jsonUtilClass);
  }

  @Provides
  @Singleton
  Properties properties() {
    final Properties copyOfProps = new Properties();
    copyOfProps.putAll(props);
    return copyOfProps;
  }
}
