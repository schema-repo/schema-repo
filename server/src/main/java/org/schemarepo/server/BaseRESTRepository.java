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

package org.schemarepo.server;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.schemarepo.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core / internal functionality common to all variants of REST repositories.
 * This primarily includes media types handling.
 */
public abstract class BaseRESTRepository {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final Repository repo;
  protected final Map<String, Renderer> rendererByMediaType;
  protected final String defaultMediaType;
  protected final List<Variant> supportedMediaTypes;

  public BaseRESTRepository(Repository repo, List<? extends Renderer> renderers) {
    this.repo = repo;
    if (repo == null) {
      throw new IllegalArgumentException("repo is null");
    }
    logger.info("Wrapping " + repo);
    if (renderers == null || renderers.isEmpty()) {
      throw new IllegalArgumentException("No renderers provided");
    }
    rendererByMediaType = new LinkedHashMap<String, Renderer>(renderers.size(), 1);
    supportedMediaTypes = new ArrayList<Variant>(renderers.size());
    for (Renderer r : renderers) {
      Renderer old = rendererByMediaType.put(r.getMediaType(), r);
      if (old != null) {
        logger.error("Renderers {} and {} both use the same media type {}", r, old, r.getMediaType());
      }
      supportedMediaTypes.add(new Variant(MediaType.valueOf(r.getMediaType()), null, null));
    }
    defaultMediaType = renderers.get(0).getMediaType();
    logger.info("Supported media types: {}", rendererByMediaType.keySet());
  }

  public String getDefaultMediaType() {
    return defaultMediaType;
  }

  protected Renderer getRenderer(String mediaType) {
    // browsers usually send more than one type separated by comma, and each type may have parameters after semicolon
    Renderer r;
    for (String singleMediaType : (mediaType == null || mediaType.isEmpty() ? WILDCARD : mediaType).split(", ?")) {
      singleMediaType = singleMediaType.split(";", 2)[0];
      r = rendererByMediaType.get(WILDCARD.equals(singleMediaType) ? getDefaultMediaType() : singleMediaType);
      if (r != null) {
        logger.debug("Handling request with Accept: {} using {}", mediaType, r);
        return r;
      }
    }
    logger.warn("No renderer configured for any of media types requested: {}, responding with the error status", mediaType);
    throw new WebApplicationException(Response.notAcceptable(supportedMediaTypes)
        .entity(format("Unsupported value of 'Accept' header: %s (supported values are %s)", mediaType, rendererByMediaType.keySet()))
        .build());
  }

}
