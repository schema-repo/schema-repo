package org.schemarepo.json;

/**
 * Test for the Gson implementation of JsonUtil.
 */
public class TestGsonJsonUtil extends TestJsonUtil<GsonJsonUtil> {
  @Override
  GsonJsonUtil createJsonUtil() {
    return new GsonJsonUtil();
  }
}
