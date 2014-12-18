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

package org.schemarepo.json;

import org.schemarepo.SchemaEntry;
import org.schemarepo.Subject;

/**
 * An API to convert back and forth with JSON.
 */
public interface JsonUtil {
  /**
   * Encode {@link org.schemarepo.Subject}s into a {@link String} for use by
   * {@link #subjectNamesFromJson(String)}
   *
   * The format is an array of objects containing a name field, for example:
   *
   * [{"name": "subject1"}, {"name": "subject2"}]
   *
   * @param subjects the Subject objects to encode
   * @return The {@link org.schemarepo.Subject} objects encoded as a String
   */
  String subjectsToJson(Iterable<Subject> subjects);

  /**
   * Decode a string created by {@link #subjectsToJson(Iterable)}
   *
   * @param str The String to decode
   * @return an {@link java.lang.Iterable} of {@link Subject}
   */
  Iterable<String> subjectNamesFromJson(String str);

  /**
   * Encode {@link org.schemarepo.SchemaEntry} objects into a {@link String} for use by
   * {@link #schemasFromJson(String)}
   *
   * The format is an array of objects containing id and schema fields, for example:
   *
   * [{"id": "0", "schema": "schema1"}, {"id": "2", "schema": "schema2"}]
   *
   * @param allEntries the SchemaEntry objects to encode
   * @return The {@link org.schemarepo.SchemaEntry} objects encoded as a String
   */
  String schemasToJson(Iterable<SchemaEntry> allEntries);

  /**
   * Decode a string created by {@link #schemasToJson(Iterable)}
   *
   * @param str The String to decode
   * @return An {@link java.lang.Iterable} of {@link SchemaEntry}
   */
  Iterable<SchemaEntry> schemasFromJson(String str);
}
