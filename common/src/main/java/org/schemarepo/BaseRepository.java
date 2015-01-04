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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all repository implementations.
 */
public abstract class BaseRepository implements Repository {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected boolean closed;

  /**
   * Asserts the repository is in a valid state (for ex. not closed).
   * @throws java.lang.IllegalStateException if the repository is NOT in a valid state
   */
  public void isValid() {
    if (closed) {
      throw new IllegalStateException("Repository is closed");
    }
  }

  @Override
  public void close() throws IOException {
    logger.info("Closing {}", this);
  }

  /**
   * Expose certain configuration elements as properties.
   * This can be used to construct a useful toString() response, for ex.
   * <p>Remember to call <pre>super</pre> when overriding!</p>
   * @return Map representing properties; note that the actual implementation may be immutable
   */
  protected Map<String, String> exposeConfiguration() {
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder().append(super.toString());
    final Map<String, String> properties = exposeConfiguration();
    if (properties != null) {
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        builder.append("\n\t").append(entry.getKey()).append("\t: ").append(entry.getValue());
      }
    }
    return builder.toString();
  }

}
