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

/**
 * Parent class of the actual backend (as opposed to decorating) repositories.
 * Each backend repository is expected to cache and validate its subjects.
 */
public abstract class AbstractBackendRepository extends BaseRepository {

  protected final InMemorySubjectCache subjectCache = new InMemorySubjectCache();
  protected final ValidatorFactory validators;

  protected AbstractBackendRepository(final ValidatorFactory validators) {
    this.validators = validators != null ? validators : ValidatorFactory.EMPTY;
  }

  /**
   * Concrete subclasses must override to return specific implementation of {@link org.schemarepo.Subject}.
   * Note that this instantiates the "right" subject only, any "side effects" (such as mutating persistent state)
   * must be implemented in {@link #registerSubjectInBackend(String, SubjectConfig)}
   * @param subjectName subject name
   * @return Subject
   */
  protected abstract Subject getSubjectInstance(final String subjectName);

  /**
   * Creates, applies validation decorator, and caches subject.
   * @param subjectName subject name
   * @return Subject the newly created instance or possibly pre-existing cached instance
   */
  protected final Subject getAndCacheSubject(final String subjectName) {
    return cacheSubject(getSubjectInstance(subjectName));
  }

  /**
   * Applies validation decorator, and caches subject.
   * @param subject subject to cache
   * @return Subject the passed instance or possibly pre-existing cached instance
   */
  protected final Subject cacheSubject(final Subject subject) {
    return subjectCache.add(Subject.validatingSubject(subject, validators));
  }

  @Override
  public synchronized Subject register(final String subjectName, final SubjectConfig config) {
    isValid();
    Subject subject = subjectCache.lookup(subjectName);
    if (subject == null) {
      registerSubjectInBackend(subjectName, config);
      subject = getAndCacheSubject(subjectName);
    } else {
      logger.debug("Subject {} already exists, reusing", subjectName);
    }
    return subject;
  }

  /**
   * Backend-specific registration logic. This is called during registration upon cache miss.
   * Responsible for the persistent "side effects".
   * @param subjectName subject name
   * @param config subject config
   */
  protected abstract void registerSubjectInBackend(final String subjectName, final SubjectConfig config);

  @Override
  public synchronized Subject lookup(final String subjectName) {
    isValid();
    Subject subject = subjectCache.lookup(subjectName);
    if (subject == null) {
      if (checkSubjectExistsInBackend(subjectName)) {
        subject = getAndCacheSubject(subjectName);
      }
    }
    return subject;
  }

  /**
   * Backend-specific check if the subject actually exists. This is called during lookup upon cache miss.
   * Usually is overridden by subclasses. By default, assume that a subject does <b>not</b> exist if it's not in cache.
   * @param subjectName subject name
   * @return boolean
   */
  protected boolean checkSubjectExistsInBackend(final String subjectName) {
    return false;
  }

  @Override
  public synchronized Iterable<Subject> subjects() {
    isValid();
    return subjectCache.values();
  }

}
