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

package org.schemarepo.zookeeper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;
import org.schemarepo.AbstractBackendRepository;
import org.schemarepo.RepositoryUtil;
import org.schemarepo.SchemaEntry;
import org.schemarepo.SchemaValidationException;
import org.schemarepo.Subject;
import org.schemarepo.SubjectConfig;
import org.schemarepo.ValidatorFactory;
import org.schemarepo.config.Config;

/**
 * This {@link org.schemarepo.Repository} implementation stores its state using Zookeeper.
 * <p/>
 * It requires the schema-repo.zookeeper.ensemble configuration property. This is
 * a comma-separated list of host:port addresses. Each address can also be suffixed
 * by a namespace, i.e.: zk.1:2181/schemas,zk.2:2181/schemas,zk.3:2181/schemas
 * <p/>
 * This Repository is meant to be highly available, meaning that multiple instances
 * can share the same Zookeeper ensemble and synchronize their state through it.
 */
public class ZooKeeperRepository extends AbstractBackendRepository {

  // Constants
  private static final String LOCKFILE = ".repo.lock";
  private static final String SUBJECT_PROPERTIES = "subject.properties";
  private static final String SCHEMA_IDS = "schema_ids";
  private static final String SCHEMA_POSTFIX = ".schema";

  // Curator implementation details
  CuratorFramework zkClient;
  InterProcessSemaphoreMutex zkLock;

  @Inject
  public ZooKeeperRepository(@Named(Config.ZK_ENSEMBLE) String zkEnsemble,
                             @Named(Config.ZK_PATH_PREFIX) String zkPathPrefix,
                             @Named(Config.ZK_SESSION_TIMEOUT) Integer zkSessionTimeout,
                             @Named(Config.ZK_CONNECTION_TIMEOUT) Integer zkConnectionTimeout,
                             @Named(Config.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES) Integer curatorSleepTimeBetweenRetries,
                             @Named(Config.ZK_CURATOR_NUMBER_OF_RETRIES) Integer curatorNumberOfRetries,
                             ValidatorFactory validators)
  {
    super(validators);

    if (zkEnsemble == null || zkEnsemble.isEmpty()) {
      logger.error("The '{}' config is missing. Exiting.", Config.ZK_ENSEMBLE);
      System.exit(1);
    }

    logger.info("Starting ZookeeperRepository with the following parameters:\n" +
            Config.ZK_ENSEMBLE + ": " + zkEnsemble + "\n" +
            Config.ZK_PATH_PREFIX + ": " + zkPathPrefix + "\n" +
            Config.ZK_SESSION_TIMEOUT + ": " + zkSessionTimeout + "\n" +
            Config.ZK_CONNECTION_TIMEOUT + ": " + zkConnectionTimeout + "\n" +
            Config.ZK_CURATOR_SLEEP_TIME_BETWEEN_RETRIES + ": " + curatorSleepTimeBetweenRetries + "\n" +
            Config.ZK_CURATOR_NUMBER_OF_RETRIES + ": " + curatorNumberOfRetries);

    RetryPolicy retryPolicy = new RetryNTimes(curatorSleepTimeBetweenRetries, curatorNumberOfRetries);
    CuratorFrameworkFactory.Builder cffBuilder = CuratorFrameworkFactory.builder()
            .connectString(zkEnsemble)
            .sessionTimeoutMs(zkSessionTimeout)
            .connectionTimeoutMs(zkConnectionTimeout)
            .retryPolicy(retryPolicy)
            .defaultData(new byte[0]);

    // This temporary CuratorFramework is not namespaced and is only used to ensure the zkPathPrefix is properly initialized
    CuratorFramework tempCuratorFramework = cffBuilder.build();
    tempCuratorFramework.start();

    try {
      tempCuratorFramework.blockUntilConnected();
      tempCuratorFramework.create().creatingParentsIfNeeded().forPath(zkPathPrefix);
      logger.info("The ZK Path Prefix ({}) was created in ZK.", zkPathPrefix);
    } catch (KeeperException.NodeExistsException e) {
      logger.info("The ZK Path Prefix ({}) was found in ZK.", zkPathPrefix);
    } catch (IllegalArgumentException e) {
      logger.error("Got an IllegalArgumentException while attempting to create the ZK Path Prefix (" + zkPathPrefix + "). Exiting.", e);
      System.exit(1);
    } catch (Exception e) {
      logger.error("There was an unrecoverable exception during the ZooKeeperRepository startup. Exiting.", e);
      System.exit(1);
    }

    // Once we're certain the zkPathPrefix is present, we initialize the CuratorFramework
    // we'll use for the rest of the ZK Repository's runtime.
    String zkPathPrefixWithoutLeadingSlash = zkPathPrefix.substring(1);
    zkClient = cffBuilder.namespace(zkPathPrefixWithoutLeadingSlash).build();
    zkClient.start();

    try {
      zkClient.blockUntilConnected();
      zkLock = new InterProcessSemaphoreMutex(zkClient, LOCKFILE);
      logger.info("ZooKeeperRepository startup finished!");
    } catch (Exception e) {
      logger.error("There was an unrecoverable exception during the ZooKeeperRepository startup. Exiting.", e);
      System.exit(1);
    }
  }

  private void acquireLock() {
    try {
      zkLock.acquire();
    } catch (Exception e) {
      logger.error("An exception occurred while trying to get the ZK lock!", e);
      throw new RuntimeException(e);
    }
  }

  private void releaseLock() {
    try {
      zkLock.release();
    } catch (Exception e) {
      logger.error("An exception occurred while trying to release the ZK lock!", e);
      throw new RuntimeException(e);
    }
  }

  protected Subject getSubjectInstance(final String subjectName) {
    return new ZooKeeperSubject(subjectName);
  }

  @Override
  protected void registerSubjectInBackend(final String subjectName, final SubjectConfig config) {
    // If the Subject is not in the local cache, we acquire the lock to create it
    acquireLock();
    try {
      zkClient.create().forPath(subjectName);
      // N.B.: There is a possibility that the Subject was already created by
      // another repository instance. If the create operation above didn't throw,
      // then we need to initialize the Subject.

      // Create schema IDs file
      zkClient.create().forPath(subjectName + "/" + SCHEMA_IDS);
      // Create properties file
      Properties props = new Properties();
      props.putAll(RepositoryUtil.safeConfig(config).asMap());
      StringWriter sw = new StringWriter();
      props.store(sw, "Schema Repository Subject Properties");
      byte[] content = sw.toString().getBytes();
      zkClient.create().forPath(subjectName + "/" + SUBJECT_PROPERTIES, content);

    } catch (KeeperException.NodeExistsException e) {
      // The Subject was already created by another repository instance, we will
      // just fetch it, below, instead of creating a new one.
    } catch (Exception e) {
      logger.error("An exception occurred while accessing ZK!", e);
      throw new RuntimeException(e);
    } finally {
      releaseLock();
    }
  }

  @Override
  protected boolean checkSubjectExistsInBackend(final String subjectName) {
    // If not in cache, another instance may have created it
    try {
      // TODO: Allow this behavior to be disabled once we have async updating
      // of the cache via ZK Observers... This would protect ZK from getting
      // hammered too much at the expense of slightly stale data.
      return zkClient.checkExists().forPath(subjectName) != null;
    } catch (Exception e) {
      logger.error("An exception occurred while accessing ZK!", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized Iterable<Subject> subjects() {
    isValid();

    try {
      // TODO: Allow this behavior to be disabled once we have async updating
      // of the cache via ZK Observers... This would protect ZK from getting
      // hammered too much at the expense of slightly stale data.
      Iterable<String> subjectsInZk = zkClient.getChildren().forPath("");
      for (String subjectInZk : subjectsInZk) {
        if (!subjectInZk.equals(LOCKFILE)) {
          if (subjectCache.lookup(subjectInZk) == null) {
            getAndCacheSubject(subjectInZk);
          }
        }
      }
    } catch (Exception e) {
      logger.error("An exception occurred while accessing ZK!", e);
      throw new RuntimeException(e);
    }

    return super.subjects();
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    Integer waitTime = 100;
    while (true) {
      if (zkLock.isAcquiredInThisProcess()) {
        try {
          logger.info("ZooKeeperRepository's close() called while lock is acquired. " +
                  "Waiting " + waitTime + " ms before trying again.");
          wait(waitTime);
        } catch (InterruptedException e) {
          logger.warn("Interrupted while waiting", e);
        }
      } else {
        // TODO: Make sure the race condition between the if condition and the close is harmless...
        zkClient.close();
        break;
      }
    }
    closed = true;
    super.close();
  }

  @Override
  public void isValid() {
    super.isValid();
    if (zkClient.getState() != CuratorFrameworkState.STARTED) {
      throw new IllegalStateException("ZK Client is not connected");
    }
  }

  @Override
  protected Map<String, String> exposeConfiguration() {
    final Map<String, String> properties = new LinkedHashMap<String, String>(super.exposeConfiguration());
    properties.put(Config.ZK_ENSEMBLE, zkClient.getZookeeperClient().getCurrentConnectionString());
    return properties;
  }


  private class ZooKeeperSubject extends Subject {
    //private final SubjectConfig config;

    /**
     * A {@link org.schemarepo.Subject} has a name. The name must not be null or empty, and
     * cannot contain whitespace. If the name contains whitespace an
     * {@link IllegalArgumentException} is thrown.
     */
    protected ZooKeeperSubject(String subjectName) {
      super(subjectName);

      try {
        if (zkClient.checkExists().forPath(subjectName) == null) {
          throw new RuntimeException("The Subject does not exist in ZK!");
        }
          Set<String> schemaFileNames = getSchemaFiles();
          Set<Integer> foundIds = new HashSet<Integer>();
          for (Integer id : getSchemaIds()) {
            if(!foundIds.add(id)) {
              throw new RuntimeException("Corrupt id file, id '" + id +
                      "' duplicated in " + getSchemaIdsFilePath());
            }
            //fileReadable(getSchemaFile(id));
            schemaFileNames.remove(getSchemaFileName(id));
          }
          if (schemaFileNames.size() > 0) {
            throw new RuntimeException("Schema files found in subject directory "
                    + getSubjectPath()
                    + " that are not referenced in the " + SCHEMA_IDS + " file: "
                    + schemaFileNames.toString());
          }
      } catch (IOException e) {
        throw new RuntimeException("An IOException occurred while reading the properties at: " +
                getConfigFilePath(), e);
      } catch (Exception e) {
        throw new RuntimeException("An exception occurred while accessing ZK!", e);
      }
    }

    private String getSchemaFileName(String schemaId) {
      return schemaId + SCHEMA_POSTFIX;
    }

    private String getSchemaFileName(int schemaId) {
      return getSchemaFileName(String.valueOf(schemaId));
    }

    private String getSubjectPath() {
      return "/" + getName();
    }

    private String getConfigFilePath() {
      return getSubjectPath() + "/" + SUBJECT_PROPERTIES;
    }

    private String getSchemaIdsFilePath() {
      return getSubjectPath() + "/" + SCHEMA_IDS;
    }

    private String getSchemaFilePath(String schemaId) {
      return getSubjectPath() + "/" + getSchemaFileName(schemaId);
    }

    private Set<String> getSchemaFiles() {
      try {
        // TODO: Allow this behavior to be disabled once we have async updating
        // of the cache via ZK Observers... This would protect ZK from getting
        // hammered too much at the expense of slightly stale data.
        List<String> filesInSubject = zkClient.getChildren().forPath(getSubjectPath());
        Set<String> schemaFiles = new HashSet<String>();
        for (String fileName: filesInSubject) {
          if (fileName.endsWith(SCHEMA_POSTFIX)) {
            schemaFiles.add(fileName);
          }
        }
        return schemaFiles;
      } catch (Exception e) {
        throw new RuntimeException("An exception occurred while accessing ZK!", e);
      }
    }

    // schema ids from the schema id file, in order from oldest to newest
    private List<Integer> getSchemaIds(){
      // TODO: Make IDs String across the board (not Integer),
      // TODO: Add pluggable ID generation schemes
      try {
        // TODO: Allow this behavior to be disabled once we have async updating
        // of the cache via ZK Observers... This would protect ZK from getting
        // hammered too much at the expense of slightly stale data.
        byte[] rawContent = zkClient.getData().forPath(getSchemaIdsFilePath());

        ArrayList<Integer> schemaIdList = new ArrayList<Integer>();

        Scanner scanner = new Scanner(new ByteArrayInputStream(rawContent));

        while (scanner.hasNext()) {
          String line = scanner.nextLine();

          try {
            Integer id = Integer.parseInt(line);
            schemaIdList.add(id);
          } catch (NumberFormatException e) {
            logger.error("Got an invalid ID ({}) in {} !", line, getSchemaIdsFilePath(), e);
          }
        }

        return schemaIdList;
      } catch (Exception e) {
        throw new RuntimeException("An exception occurred while accessing ZK!", e);
      }
    }

    private Integer getLatestSchemaId(List<Integer> currentSchemaIds) {
      // TODO: Make IDs String across the board (not Integer),
      // TODO: Add pluggable ID generation schemes
      Integer lastId = -1;
      for (Integer id : currentSchemaIds) {
        if (id > lastId) {
          lastId = id;
        }
      }
      return lastId;
    }

    private Integer getLatestSchemaId() {
      return getLatestSchemaId(getSchemaIds());
    }

    private String readSchemaForId(String schemaId) {
      try {
        byte[] rawContent = zkClient.getData().forPath(getSchemaFilePath(schemaId));
        if (rawContent == null || rawContent.length == 0) {
          return null;
        } else {
          return new String(rawContent);
        }
      } catch (KeeperException.NoNodeException e) {
        // The schema for this ID does not exist in ZK.
        return null;
      } catch (Exception e) {
        throw new RuntimeException("An exception occurred while accessing ZK!", e);
      }
    }

    private final String endOfLine = System.getProperty("line.separator");

    private String serializeSchemaIds(List<Integer> schemaIds) {
      // TODO: Make IDs String across the board (not Integer),
      // TODO: Add pluggable ID generation schemes
      StringBuilder sb = new StringBuilder();
      boolean firstLine = true;
      for (Integer id: schemaIds) {
        if (firstLine) {
          firstLine = false;
        } else {
          sb.append(endOfLine);
        }
        sb.append(id.toString());
      }
      return sb.toString();
    }

    private synchronized SchemaEntry createNewSchema(String schema) {
      try {
        // TODO: Make IDs String across the board (not Integer),
        // TODO: Add pluggable ID generation schemes
        List<Integer> allSchemaIds = getSchemaIds();
        Integer newId = getLatestSchemaId(allSchemaIds) + 1;
        allSchemaIds.add(newId);
        byte[] newSchemaFile = schema.getBytes();
        byte[] newSchemaIdsFile = serializeSchemaIds(allSchemaIds).getBytes();
        // Create new schema and update schema IDs file in one ZK transaction
        zkClient.inTransaction().
                create().forPath(getSchemaFilePath(newId.toString()), newSchemaFile).
                and().
                setData().forPath(getSchemaIdsFilePath(), newSchemaIdsFile).
                and().commit();

          // TODO: Keep new schema in a local cache
          return new SchemaEntry(String.valueOf(newId), schema);
      } catch (Exception e) {
        throw new RuntimeException(
                "An exception occurred while accessing ZK!", e);
      }
    }

    /**
     * @return The {@link org.schemarepo.SubjectConfig} for this Subject
     */
    @Override
    public SubjectConfig getConfig() {
      try {
        // TODO: Allow this behavior to be disabled once we have async updating
        // of the cache via ZK Observers... This would protect ZK from getting
        // hammered too much at the expense of slightly stale data.
        Properties props = new Properties();
        byte[] rawProperties = zkClient.getData().forPath(getConfigFilePath());
        props.load(new ByteArrayInputStream(rawProperties));
        return RepositoryUtil.configFromProperties(props);
      } catch (Exception e) {
        throw new RuntimeException("An exception occurred while accessing ZK!", e);
      }
    }

    /**
     * Indicates whether the keys generated by this subject can be expected to parse
     * as an integer. This delegates all the way through to the backing store and
     * is not configurable through the Repository/Subject API, since implementations
     * of the backing store are what determines how keys are generated; the contract
     * otherwise is merely that they are Strings and unique per subject.
     *
     * @return a boolean indicating if the IDs for this Subject are integers
     */
    @Override
    public boolean integralKeys() {
      // TODO: Make IDs String across the board (not Integer),
      // TODO: Add pluggable ID generation schemes
      return true;
    }

    /**
     * If the provided schema has already been registered in this subject, return
     * the id.
     * <p/>
     * If the provided schema has not been registered in this subject, register it
     * and return its id.
     * <p/>
     * Idempotent -- If two users simultaneously register the same schema, they
     * will both get the same {@link org.schemarepo.SchemaEntry} result and succeed.
     *
     * @param schema The schema to register
     * @return The id of the schema
     * @throws org.schemarepo.SchemaValidationException
     *          If the schema change is not valid according the validation rules
     *          of the subject
     */
    @Override
    public SchemaEntry register(String schema) throws SchemaValidationException {
      RepositoryUtil.validateSchemaOrSubject(schema);
      SchemaEntry cachedSchema = null; // TODO: lookup within local cache first
      if (cachedSchema != null) {
        return cachedSchema;
      } else {
        acquireLock();
        SchemaEntry entry = lookupBySchema(schema);
        if (entry == null) {
          entry = createNewSchema(schema);
        }
        releaseLock();
        return entry;
      }
    }

    /**
     * Register the provided schema only if the current latest schema matches the
     * provided latest entry.
     *
     * @param schema The schema to register
     * @param latest the entry that must match the current actual latest value in order
     *               to register this schema.
     * @return The id of the schema, or null if latest does not match.
     * @throws org.schemarepo.SchemaValidationException
     *          If the schema change is not valid according the validation rules
     *          of the subject
     */
    @Override
    public SchemaEntry registerIfLatest(String schema, SchemaEntry latest) throws SchemaValidationException {
      SchemaEntry latestInZk = latest();
      if (latest == latestInZk // both null
              || (latest != null && latest.equals(latestInZk))) {
        return register(schema);
      } else {
        return null;
      }
    }

    /**
     * Lookup the {@link org.schemarepo.SchemaEntry} for the given schema. Since the mapping of
     * schema to id is immutable, this result can be cached.
     *
     * @param schema The schema to look up
     * @return The SchemaEntry of the schema or null if the schema is not
     *         registered
     */
    @Override
    public SchemaEntry lookupBySchema(String schema) {
      RepositoryUtil.validateSchemaOrSubject(schema);
      for (Integer id : getSchemaIds()) {
        String idStr = id.toString();
        String schemaInFile = readSchemaForId(idStr);
        if (schema.equals(schemaInFile)) {
          return new SchemaEntry(idStr, schema);
        }
      }
      return null;
    }

    /**
     * Lookup the {@link org.schemarepo.SchemaEntry} for the given subject by id. Since the
     * mapping of schema to id is immutable the result can be cached.
     *
     * @param id the id of the schema to look up
     * @return The SchemaEntry of the schema or null if no such schema is
     *         registered for the provided id
     */
    @Override
    public SchemaEntry lookupById(String id) {
      SchemaEntry cachedSchema = null; // TODO: lookup within local cache first
      if (cachedSchema != null) {
        return cachedSchema;
      } else {
        String schema = readSchemaForId(id);
        if (schema != null) {
          return new SchemaEntry(id, schema);
        }
        return null;
      }
    }

    /**
     * Lookup the most recently registered schema for the given subject. This
     * result is not cacheable, since the latest schema may change.
     *
     * @return The {@link org.schemarepo.SchemaEntry} or null if no schema is registered with
     *         this subject
     */
    @Override
    public SchemaEntry latest() {
      // TODO: Make IDs String across the board (not Integer),
      // TODO: Add pluggable ID generation schemes
      Integer latestId = getLatestSchemaId(); // This part is not cacheable
      SchemaEntry cachedSchema = null; // TODO: lookup within local cache first
      if (cachedSchema != null) {
        return cachedSchema;
      } else {
        String latestSchemaLiteral = readSchemaForId(latestId.toString());
        if (latestSchemaLiteral == null) {
          return null;
        } else {
          return new SchemaEntry(latestId.toString(), latestSchemaLiteral);
        }
      }
    }

    /**
     * List the ids of schemas registered with the given subject, ordered from
     * most recent to oldest. This result is not cacheable, since the
     * {@link org.schemarepo.SchemaEntry} in the subject may grow over time.
     *
     * @return the {@link org.schemarepo.SchemaEntry} objects in this subject, ordered from most
     *         recent to oldest.
     */
    @Override
    public Iterable<SchemaEntry> allEntries() {
      // TODO: Get known schemas from local cache.
      // TODO: Only touch ZK for list of schema IDs and unknown schemas within that list
      List<SchemaEntry> entries = new ArrayList<SchemaEntry>();
      for (Integer id : getSchemaIds()) {
        String idStr = id.toString();
        String schema = readSchemaForId(idStr);
        entries.add(new SchemaEntry(idStr, schema));
      }
      Collections.reverse(entries);
      return entries;
    }
  }
}
