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

import org.apache.avro.Schema;

/**
 * This converter can be used for handling Avro schemas.
 */
public class AvroSchemaConverter implements Converter<Schema> {

  private final Boolean prettyPrint;

  public AvroSchemaConverter() {
    this(true);
  }

  public AvroSchemaConverter(Boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * Given a String literal, provide a strongly-typed instance.
   *
   * @param literal to be converted
   * @return the requested TYPE
   */
  @Override
  @SuppressWarnings("deprecation")
  public Schema fromString(String literal) {
    // Non-deprecated code for Avro 1.7.x :
    // return new Schema.Parser().parse(literal);

    // N.B.: Willfully using the deprecated Avro API to maintain
    // compatibility with older Avro jars.
    return Schema.parse(literal);
  }

  /**
   * Given a strongly-typed instance, provide its String literal representation.
   *
   * @param strongType instance to be converted
   * @return the String literal representation
   */
  @Override
  public String toString(Schema strongType) {
    return strongType.toString(prettyPrint);
  }
}
