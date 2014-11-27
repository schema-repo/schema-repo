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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLocalFileSystemRepository extends AbstractTestPersistentRepository<LocalFileSystemRepository> {
  private static final String TEST_PATH = "target/test/TestLocalFileSystemRepository-paths/";
  private static final String REPO_PATH = "target/test/TestLocalFileSystemRepository/";

  @BeforeClass
  public static void setup() {
    rmDir(new File(TEST_PATH));
    rmDir(new File(REPO_PATH));
  }

  @After
  public void cleanUp() throws IOException {
    getRepo().close();
    // Clean up the repo's content
    rmDir(new File(REPO_PATH));
  }

  @Override
  protected LocalFileSystemRepository createRepository() {
    return newRepo(REPO_PATH);
  }

  private LocalFileSystemRepository newRepo(String path) {
      return new LocalFileSystemRepository(path, new ValidatorFactory.Builder().build());
  }

  @Test
  public void testPathHandling() throws SchemaValidationException {
    String paths[] = new String[] {
        "data", "data/", "/tmp/file_repo",
        "/tmp/file_repo/", "/tmp/file_repo/" };

    for (String path : paths) {
      LocalFileSystemRepository r = newRepo(TEST_PATH + path);
      try {
        File expected = new File(TEST_PATH, path);
        assertTrue("Expected directory not created: " +
        expected.getAbsolutePath() + " for path: " + path, expected.exists());
      } finally {
        r.close();
        // should be ok to call close twice
        r.close();
      }
    }
    // verify idempotent
    newRepo(TEST_PATH + "/tmp/repo").close();
    newRepo(TEST_PATH + "/tmp/repo").close();
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidDir() throws IOException {
    String badPath = TEST_PATH + "/bad";
    new File(TEST_PATH).mkdirs();
    new File(badPath).createNewFile();
    LocalFileSystemRepository r = newRepo(badPath);
    r.close();
  }

  @Test(expected = IllegalStateException.class)
  public void testCantUseClosedRepo() {
    LocalFileSystemRepository r = newRepo(TEST_PATH + "/tmp/repo");
    r.close();
    r.lookup("nothing");
  }

  private static void rmDir(File dir) {
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    for (String filename : dir.list()) {
      File entry = new File(dir, filename);
      if (entry.isDirectory()) {
        rmDir(entry);
      } else {
        entry.delete();
      }
    }
    dir.delete();
  }
}
