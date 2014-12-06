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

import java.io.Closeable;
import java.util.Properties;

/**
 * A {@link Repository} is a collection of {@link Subject}s. A {@link Subject}
 * can be looked up by name on a {@link Repository}, or registered.<br/>
 * <br/>
 * Registration of a {@link Subject} in a {@link Repository} is done via
 * {@link #register(String, SubjectConfig)}, which requires the subject
 * name and its configuration. The configuration is a map of configuration
 * keys to configuration values, both of which are Strings.<br/>
 * <br/>
 *
 */
public interface Repository extends Closeable {

  /**
   * Attempt to create a Subject with the given name and validator.
   *
   * @param subjectName
   *          The name of the subject. Must not be null.
   * @param config
   *          The subject configuration. May be null.
   * @return The newly created Subject, or an equivalent one if already created.
   *         Does not return null.
   * @throws NullPointerException
   *           if subjectName is null
   */
  Subject register(String subjectName, SubjectConfig config);

  /**
   * Returns the subject if it exists, null otherwise.
   *
   * @param subjectName
   *          the subject name
   * @return The subject if it exists, null otherwise.
   */
  Subject lookup(String subjectName);

  /**
   * List all subjects. Does not return null.
   */
  Iterable<Subject> subjects();

  /**
   * Returns the status of the repository.
   * The actual specific of <b>what</b> is included into the response and
   * <b>how</b> is it represented (e.g. what's the format of the string)
   * are specific to each repository implementation.
   * @return String status data
   */
  String getStatus();

  /**
   * Provides access to repository's configuration.
   * @param includeDefaults when false, only the properties explicitly set by repository's creator
   *                        are returned; otherwise default values are included for those not explicitly set
   * @return Properties repository's configuration
   */
  Properties getConfiguration(boolean includeDefaults);

}
