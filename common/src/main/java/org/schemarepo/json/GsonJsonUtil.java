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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.schemarepo.SchemaEntry;
import org.schemarepo.Subject;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * An implementation of JsonUtil that uses google-gson.
 */
public class GsonJsonUtil implements JsonUtil {

  private class SubjectRepresentation {
    private String name;

    SubjectRepresentation(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private static final Gson GSON_FOR_SUBJECTS = new GsonBuilder()
          .setPrettyPrinting()
          .serializeNulls()
          .setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
              // We're only interested in the name field for subjects
              return (fieldAttributes.getName() != "name");
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
              return false;
            }
          })
          .create();

  private static final Gson GSON = new GsonBuilder()
          .setPrettyPrinting()
          .create();

  private static final Type SUBJECT_LIST_TYPE =
          new TypeToken<List<SubjectRepresentation>>() {}.getType();

  private static final Type SCHEMA_ENTRY_LIST_TYPE =
          new TypeToken<List<SchemaEntry>>() {}.getType();

  @Override
  public String subjectsToJson(Iterable<Subject> subjects) {
    return GSON_FOR_SUBJECTS.toJson(subjects);
  }

  @Override
  public Iterable<String> subjectNamesFromJson(String str) {
    ArrayList<String> stringList = new ArrayList<String>();

    if (str != null && !str.equals("")) {
      ArrayList<SubjectRepresentation> subjectList =
              GSON_FOR_SUBJECTS.fromJson(str, SUBJECT_LIST_TYPE);
      for (SubjectRepresentation subject : subjectList) {
        stringList.add(subject.getName());
      }
    }

    return stringList;
  }

  @Override
  public String schemasToJson(Iterable<SchemaEntry> allEntries) {
    return GSON.toJson(allEntries);
  }

  @Override
  public Iterable<SchemaEntry> schemasFromJson(String str) {
    if (str == null || str.equals("")) {
      return new ArrayList<SchemaEntry>();
    }
    return GSON.fromJson(str, SCHEMA_ENTRY_LIST_TYPE);
  }
}
