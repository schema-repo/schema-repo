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

package org.schemarepo.client;

import org.junit.Ignore;
import org.junit.Test;

public class TestAvro1124RESTRepositoryClient
        extends AbstractTestRESTRepositoryClient<Avro1124RESTRepositoryClient> {

  @Override
  protected Avro1124RESTRepositoryClient createClient(String repoUrl) {
    return new Avro1124RESTRepositoryClient(repoUrl);
  }

  @Test
  @Ignore("This test is skipped because we know the old client is broken " +
          "when calling allEntries() for schemas with new lines.")
  public void testAllEntriesMultiLineSchema() throws Exception {
    // Skipped
  }
}
