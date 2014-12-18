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
 * To convert back and forth with Byte.
 *
 * For most people this can be a reasonable choice for IDs. Most use cases
 * should require less than 256 schemas per subject. However, if one wants to be
 * extra paranoid about future extensibility, the ShortConverter should provide
 * as much mileage as one might need.
 */
public class ByteConverter implements Converter<Byte> {
  @Override
  public Byte fromString(String literal) {
    return Byte.parseByte(literal);
  }

  @Override
  public String toString(Byte strongType) {
    return strongType.toString();
  }
}
