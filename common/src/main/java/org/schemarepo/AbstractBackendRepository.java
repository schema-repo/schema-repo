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

import java.util.Collections;

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
   * Concrete subclasses must override to return specific implementation of {@link org.schemarepo.Subject}
   * @param subjectName subject name
   * @param config subject config
   * @return Subject
   */
  protected abstract Subject createSubjectInternal(final String subjectName, final SubjectConfig config);

  /**
   * Creates, applies validation decorator, and caches subject.
   * @param subjectName subject name
   * @param config subject config
   * @return Subject the newly created instance or possibly pre-existing cached instance
   */
  protected final Subject createAndCacheSubject(final String subjectName, final SubjectConfig config) {
    return subjectCache.add(Subject.validatingSubject(createSubjectInternal(subjectName, config), validators));
  }

  @Override
  public synchronized Subject register(final String subjectName, final SubjectConfig config) {
    isValid();
    Subject subject = subjectCache.lookup(subjectName);
    if (subject == null) {
      registerInternal(subjectName, config);
      subject = createAndCacheSubject(subjectName, config);
    } else {
      logger.debug("Subject {} already exists, reusing", subjectName);
    }
    return subject;
  }

  /**
   * Backend-specific registration logic. This is called during registration upon cache miss.
   * Usually is overridden by subclasses. By default, do nothing.
   * @param subjectName subject name
   * @param config subject config
   */
  protected void registerInternal(final String subjectName, final SubjectConfig config) {
  }

  @Override
  public synchronized Subject lookup(final String subjectName) {
    isValid();
    Subject subject = subjectCache.lookup(subjectName);
    if (subject == null) {
      if (checkSubjectExistsInternal(subjectName)) {
        subject = createAndCacheSubject(subjectName, null);
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
  protected boolean checkSubjectExistsInternal(final String subjectName) {
    return false;
  }

  @Override
  public synchronized Iterable<Subject> subjects() {
    isValid();
    Iterable<String> subjectNames = fetchSubjectsInternal();
    subjectNames = subjectNames != null ? subjectNames : Collections.EMPTY_SET;
    for (String subjectName : subjectNames) {
      createAndCacheSubject(subjectName, null);
    }
    return subjectCache.values();
  }

  /**
   * Backend-specific implementation of fetching all existing subjects.
   * Default implementation assumes the cache is always up-to-date.
   * @return Set set of subject names, null or empty set if nothing exists
   */
  protected Iterable<String> fetchSubjectsInternal() {
    return null;
  }

}
