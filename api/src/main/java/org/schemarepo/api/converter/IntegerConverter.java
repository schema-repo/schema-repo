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

package org.schemarepo.api.converter;

/**
 * To convert back and forth with Integer.
 *
 * As a converter for IDs, this is probably overkill for most use cases, since
 * it allows billions of schemas per subject. There may be some highly generic
 * and dynamic system architectures that would warrant such high cardinality,
 * but for most intents and purposes, one should probably think twice about
 * using this kind of ID.
 */
public class IntegerConverter implements Converter<Integer> {
  @Override
  public Integer fromString(String literal) {
    return Integer.parseInt(literal);
  }

  @Override
  public String toString(Integer strongType) {
    return strongType.toString();
  }
}
