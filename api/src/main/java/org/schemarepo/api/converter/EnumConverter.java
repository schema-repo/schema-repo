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
 * This converter can be useful for people who wish to constrain the usable
 * subjects to a predetermined set of elements, i.e.: an Enum.
 */
public class EnumConverter<E extends Enum<E>> implements Converter<E> {

  private Class<E> enumClass;

  public EnumConverter(E enumInstance) {
    this.enumClass = enumInstance.getDeclaringClass();
  }

  public EnumConverter(Class<E> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public E fromString(String literal) {
    return Enum.valueOf(enumClass, literal);
  }

  @Override
  public String toString(E strongType) {
    return strongType.name();
  }
}
