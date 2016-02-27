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

package org.schemarepo;

import org.schemarepo.config.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A factory for mapping Validator names to instantiated instances. Validator
 * names starting with "repo."
 */
public class ValidatorFactory {

  public static final ValidatorFactory EMPTY = new Builder().build();

  private final HashMap<String, Validator> validators;
  private final Set<String> defaultSubjectValidators;

  private ValidatorFactory(HashMap<String, Validator> validators, Set<String> defaultSubjectValidators) {
    this.validators = validators;
    this.defaultSubjectValidators = defaultSubjectValidators;
  }

  /**
   * @param validatorNames
   *          The set of {@link Validator} names to resolve. Must not be null.
   * @return A list of {@link Validator}s. Not null.
   */
  public final List<Validator> getValidators(Set<String> validatorNames) {
    ArrayList<Validator> result = new ArrayList<Validator>();
    for (String name : validatorNames) {
      Validator v = validators.get(name);
      if (v != null) {
        result.add(v);
      }
    }
    return result;
  }

  public final Set<String> getDefaultSubjectValidators() {
    HashSet<String> result = new HashSet<String>(defaultSubjectValidators.size());
    for (String name : defaultSubjectValidators) {
      if (validators.containsKey(name))
        result.add(name);
    }
    return result;
  }

  public static class Builder {
    private final HashMap<String, Validator> validators;
    {
      validators = new HashMap<String, Validator>();
      validators.put(Config.REJECT_VALIDATOR, new Reject());
    }

    private final Set<String> defaultSubjectValidators = new HashSet<String>();

    /**
     * Configure this builder to return a {@link ValidatorFactory} that maps the
     * {@link Validator} provided to the name given. <br/>
     * The name must not be null and must not start with "repo.".
     */
    public Builder setValidator(String name, Validator validator) {
      if (name.startsWith(Config.GLOBAL_PREFIX)) {
        throw new RuntimeException("Validator names starting with 'repo.'"
            + " are reserved.  Attempted to set validator with name: " + name);
      }
      validators.put(name, validator);
      return this;
    }

    public Builder setDefaultValidator(String name) {
      defaultSubjectValidators.add(name);
      return this;
    }

    public Builder setDefaultValidators(Collection<String> validatorNames) {
      for (String name : validatorNames) {
        setDefaultValidator(name);
      }
      return this;
    }

    public ValidatorFactory build() {
      return new ValidatorFactory(new HashMap<String, Validator>(validators), new HashSet<String>(defaultSubjectValidators));
    }
  }

  private static class Reject implements Validator {
    @Override
    public void validate(String schemaToValidate,
        Iterable<SchemaEntry> schemasInOrder) throws SchemaValidationException {
      throw new SchemaValidationException(
          "repo.validator.reject validator always rejects validation");
    }
  }
}
