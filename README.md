# Schema Repo

The schema repo is a RESTful web service for storing and serving mappings between schema identifiers and schema definitions. Those mappings are meant to be immutable, since data serialized with a given identifier should be de-serializable forever.

The primary (and initial) use case for having a schema repo is to ease the serialization and de-serialization of Avro payloads within Kafka messages, however, the schema repo is actually protocol-agnostic and does not strictly require Avro.

Please read the [AVRO-1124](https://issues.apache.org/jira/browse/AVRO-1124) ticket for more background information.

Please subscribe to the [mailing list](https://groups.google.com/forum/#!forum/schema-repo) to ask questions or discuss development.

## Build and Run

In order to build and run the schema repo, execute the following commands in the current directory:

    $ mvn install
    $ ./run.sh

## Maven Artifacts

Maven artifacts for the schema repo are published on Sonatype Central Repository, starting with release 0.1.1:

https://oss.sonatype.org/content/repositories/releases/org/schemarepo/

See the official [instructions for integrating with various build tools](https://oss.sonatype.org/content/repositories/releases/org/schemarepo/).

## Configuration

The schema repo gets configured via a .properties file passed as the first command line argument to the main method. This file can be configured with the following properties:

    # FQCN (fully qualified class name) of the schema repo backend implementation to be used:
    schema-repo.class=org.schemarepo.InMemoryRepository
     
    # FQCN of the schema repo cache implementation to be used:
    schema-repo.cache=org.schemarepo.InMemoryCache
     
    # FQCN of the validators to use. You can specify zero, one or more than one implementation, all of which need to be prefixed with 'schema-repo.validator.' : 
    schema-repo.validator.my_custom_validator_1=com.xyz.Validator1
    schema-repo.validator.my_custom_validator_2=com.xyz.Validator2
    schema-repo.validator.my_custom_validator_3=com.xyz.Validator3
     
    # Default validators to apply to all new topics where no validators are explicitly specified. Comma-separated list of Validator names (without the 'schema-repo.validator.' prefix).
    schema-repo.validation.default.validators=my_custom_validator_1,my_custom_validator_2

All configuration properties are injected via Guice. However, you are not obligated to use Guice if you do not wish to. You can also feed the required properties to the various constructors directly by code, if you wish to wire in your own config management solution.
    
### Local File System Backend

The local file system backend is a single node, persistent, implementation. For production usage, it is recommended to at least use this backend, and not the in-memory one, otherwise a server shutdown or crash will result in the loss of all of its state.

This file-based backend, however, is not considered highly-available nor fault-tolerant. Even if you could somehow set its storage path to be on a mounted file system that you would consider to be highly-available, the current implementation establishes a lock in the file system for the whole duration of the schema repo's runtime, which fences out other instances from sharing the file system.

In order to use the file-based backend, set these configuration properties:

    # FQCN of the file-based backend:
    schema-repo.class=org.schemarepo.LocalFileSystemRepository
     
    # Relative or absolute path to where you wish to store the state of the repo:
    schema-repo.local-file-system.path=relative/path/to/storage/directory/

### ZooKeeper Backend

The ZooKeeper backend stores its state in a ZooKeeper ensemble. This backend implementation is meant to be highly-available, meaning that multiple instances can share the same ZooKeeper ensemble and synchronize their state through it. All mutation operations to the schema repo's state are shielded behind a shared lock which is acquired temporarily for the time of the mutation and released afterwards.

Disclaimer: the ZooKeeper backend is still considered experimental.

In order to use the ZooKeeper-based backend, set these configuration properties:

    # FQCN of the ZooKeeper-based backend:
    schema-repo.class=org.schemarepo.zookeeper.ZooKeeperRepository
     
    # Comma-separated list of the ZK hosts and ports:
    schema-repo.zookeeper.ensemble=zk-host-1:2181,zk-host-2:2181,zk-host-3:2181
     
    # These additional properties can also be set if the defaults (shown below) need to be overridden:
    schema-repo.zookeeper.path-prefix=/schema-repo
    schema-repo.zookeeper.session-timeout=5000
    schema-repo.zookeeper.connection-timeout=2000
    schema-repo.zookeeper.curator.sleep-time-between-retries=2000
    schema-repo.zookeeper.curator.number-of-retries=10
    
### Jetty Config

The schema repo's REST server implementation uses Jetty. The defaults below will be used if these configs are not specifically overridden in the config file:

    # Jetty configs and their defaults:
    schema-repo.jetty.host=
    schema-repo.jetty.port=2876
    schema-repo.jetty.header.size=16384
    schema-repo.jetty.buffer.size=16384
    schema-repo.jetty.stop-at-shutdown=true
    schema-repo.jetty.graceful-shutdown=3000
    
## REST API Documentation

The REST endpoints supported by the Schema Repo, their descriptions, as well as example command executions and reponses are documented on the [Service Endpoints wiki page](https://github.com/schema-repo/schema-repo/wiki/Service-Endpoints).

## Reading List

Here are some interesting resources to get a better understanding of the Schema Repo's motivation and related technologies:
* [High Volume Data and Schema Evolution](https://prezi.com/dynn9skazbty/high-volume-data-and-schema-evolution/), by Scott Carey.
* [Schema evolution in Avro, Protocol Buffers and Thrift](http://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html), by Martin Kleppmann.
* The original [AVRO-1124](https://issues.apache.org/jira/browse/AVRO-1124) ticket, by Jay Kreps.

## Origin Story

The schema repo is a standalone version of the patch submitted on the Apache [AVRO-1124](https://issues.apache.org/jira/browse/AVRO-1124) ticket.

The patch was originally submitted by Jay Kreps and later on substantially refactored by Scott Carey. Some other people then contributed minor fixes and improvements.

The schema repo was separated into a standalone project because it is unclear that Apache Avro is an appropriate parent project for containing it. It was given its own repository in order to ease further development. The whole project is Apache-licensed, so any OSS project can choose to use (or even include) the schema repo.
