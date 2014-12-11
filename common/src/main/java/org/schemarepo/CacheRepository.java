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

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CacheRepository is a {@link Repository} implementation that wraps another
 * {@link Repository} and acts as a write-through cache of {@link Subject}s and
 * schema to id mappings, shielding the inner {@link Repository} from repetitive
 * lookups.
 *
 * CacheRepository can cache Subjects (which cannot be deleted) and returns an
 * implementation of {@link Subject} that caches schema to id mappings.
 *
 * CacheRepository can only cache the immutable elements of a Repository, because
 * it is intended for use in any context -- in a client, in a proxy, or above a raw
 * implementation of a repository.
 * It cannot cache the entire list of subjects since the list is mutable.
 * Similarly, a cached subject cannot cache the list of schemas, the subject configuration,
 * or the latest() schema because those are mutable.
 *
 */
public class CacheRepository implements Repository {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Repository repo;
  private final RepositoryCache cache;

  /**
   * The runtimeID is a simple sanity check to validate that the repo that gets
   * constructed by the dependency injection framework is the same that gets
   * closed at the end of the runtime.
   */
  private final double runtimeID = Math.random();

  /**
   * Create a caching repository that wraps the provided repository using the
   * cache provided
   * @param repo The repository to wrap
   * @param cache The cache to use
   */
  @Inject
  public CacheRepository(Repository repo, RepositoryCache cache) {
    this.repo = repo;
    this.cache = cache;
    logger.info("Constructed {}", this);
  }

  @Override
  public Subject register(String subjectName, SubjectConfig config) {
    Subject s = cache.lookup(subjectName);
    if (s == null) {
      return cache.add(repo.register(subjectName, config));
    }
    return s;
  }

  @Override
  public Subject lookup(String subjectName) {
    Subject s = cache.lookup(subjectName);
    if (s == null) {
      return cache.add(repo.lookup(subjectName));
    }
    return s;
  }

  @Override
  public Iterable<Subject> subjects() {
    // the full list of subjects cannot be cached.
    // however we can populate the cache with the result
    Iterable<Subject> subs = repo.subjects();
    for (Subject s : subs) {
      cache.add(s);
    }
    return subs;
  }

  @Override
  protected void exposeConfiguration(final Map<String, String> properties) {
    super.exposeConfiguration(properties);
    properties.put("cache", cache.toString());
  }

}
