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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link SubjectConfig} is effectively a Map<String, String> , with reserved
 * keys and default values for certain keys.
 * <br/>
 * Keys starting with "schema-repo." are reserved.
 *
 */
public class SubjectConfig {
  private static final SubjectConfig EMPTY = new Builder().build();
  public static final String VALIDATORS_KEY = Config.VALIDATOR_PREFIX + "validators";

  private final Map<String, String> conf;
  private final Set<String> validators;

  private SubjectConfig(Map<String, String> conf, Set<String> validators) {
    this.conf = conf;
    this.validators = validators;
  }

  public String get(String key) {
    return conf.get(key);
  }

  public Set<String> getValidators() {
    return validators;
  }

  public Map<String, String> asMap() {
    return conf;
  }

  public static SubjectConfig emptyConfig() {
    return EMPTY;
  }

  @Override
  public int hashCode() {
    return conf.hashCode() * 31 + validators.hashCode();
   }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SubjectConfig other = (SubjectConfig) obj;
    if (!validators.equals(other.validators))
      return false;
    if (!conf.equals(other.conf))
      return false;
    return true;
  }

  public static class Builder {
    private static final String RESERVED_PREFIX = Config.GLOBAL_PREFIX;

    private final HashMap<String, String> conf = new HashMap<String, String>();
    private final HashSet<String> validators = new HashSet<String>();

    public Builder set(Map<String, String> config) {
      for(Map.Entry<String, String> entry : config.entrySet()) {
        set(entry.getKey(), entry.getValue());
      }
      return this;
    }

    public Builder set(String key, String value) {
      if(key.startsWith(RESERVED_PREFIX)) {
        if(VALIDATORS_KEY.equals(key)) {
          setValidators(RepositoryUtil.commaSplit(value));
        } else {
          throw new RuntimeException("SubjectConfig keys starting with '" +
              RESERVED_PREFIX + "' are reserved, failed to set: " + key +
              " to value: " + value);
        }
      } else {
        conf.put(key, value);
      }
      return this;
    }

    public Builder setValidators(Collection<String> validatorNames) {
      this.validators.clear();
      this.conf.remove(VALIDATORS_KEY);
      if(!validatorNames.isEmpty()) {
        this.validators.addAll(validatorNames);
      }
      // put the config entry even if they specified an empty list of validators. This is explicitly "no validators"
      this.conf.put(VALIDATORS_KEY, RepositoryUtil.commaJoin(validators));
      return this;
    }

    public Builder addValidator(String validatorName) {
      this.validators.add(validatorName);
      this.conf.put(VALIDATORS_KEY, RepositoryUtil.commaJoin(validators));
      return this;
    }

    public SubjectConfig build() {
      return new SubjectConfig(
          Collections.unmodifiableMap(new HashMap<String, String>(conf)),
          Collections.unmodifiableSet(new HashSet<String>(validators)));
    }

  }


}
