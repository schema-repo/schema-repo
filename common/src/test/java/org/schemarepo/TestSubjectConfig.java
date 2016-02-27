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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.schemarepo.config.Config;


public class TestSubjectConfig {

  @Test
  public void testBuilder() {
    SubjectConfig conf = SubjectConfig.emptyConfig();
    Assert.assertTrue(conf.getValidators().isEmpty());

    SubjectConfig custom = new SubjectConfig.Builder()
      .set("k", "v")
      .set(SubjectConfig.VALIDATORS_KEY, "valid1, valid2 ,,")
      .addValidator("oneMore")
      .build();

    Assert.assertEquals("v", custom.get("k"));
    Set<String> validators = custom.getValidators();
    Assert.assertEquals(3, validators.size());
    Assert.assertTrue(validators.contains("valid1"));
    Assert.assertTrue(validators.contains("valid2"));
    Assert.assertTrue(validators.contains("oneMore"));
  }

  @Test
  public void testBuilderHashAndEquals() {
    SubjectConfig empty = SubjectConfig.emptyConfig();
    Assert.assertEquals(empty, empty);
    SubjectConfig conf = new SubjectConfig.Builder().build();
    Assert.assertEquals(empty, conf);
    SubjectConfig conf2 = new SubjectConfig.Builder()
      .set(SubjectConfig.VALIDATORS_KEY, null)
      .build();

    // Explicitly setting empty or null validators is NOT the same as no validators
    // due to the default subject validators functionality (see Issue 38)
    Assert.assertNotEquals(conf, conf2);
    Assert.assertNotEquals(conf2, empty);
    Assert.assertNotEquals(conf.hashCode(), conf2.hashCode());

    Assert.assertFalse(conf.equals(null));
    Assert.assertFalse(conf.equals(new Object()));
    Assert.assertEquals(conf.hashCode(), empty.hashCode());

    String k = "key";
    String v = "val";
    SubjectConfig custom = new SubjectConfig.Builder()
      .set(k, v).build();
    SubjectConfig custom2 = new SubjectConfig.Builder()
      .set(custom.asMap()).build();
    SubjectConfig custom3 = new SubjectConfig.Builder()
    .set(custom.asMap()).addValidator("foo").build();
    Assert.assertEquals(custom, custom2);
    Assert.assertFalse(custom.equals(custom3));
    Assert.assertFalse(custom.equals(conf));
    Assert.assertEquals(custom.hashCode(), custom2.hashCode());

  }

  @Test(expected=RuntimeException.class)
  public void testInvalidConfigName() {
    new SubjectConfig.Builder()
    .set(Config.GLOBAL_PREFIX + "notValid", "");
  }

}
