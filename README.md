# Schema Repo

The Schema Repo is a RESTful web service for storing and serving mappings between schema identifiers and schema definitions. Those mappings are meant to be immutable, since data serialized with a given identifier should be de-serializable forever.

The primary (and initial) use case for having a schema repo is to ease the serialization and de-serialization of Avro payloads within Kafka messages, however, the schema repo is actually protocol-agnostic and does not strictly require Avro.

Please read the [AVRO-1124](https://issues.apache.org/jira/browse/AVRO-1124) ticket for more information.

## Build and run

In order to build and run the schema repo, execute the following commands in the current directory:

    $ mvn install
    $ ./run.sh
    
## Origin story

The schema repo is a standalone version of the patch submitted on the Apache [AVRO-1124](https://issues.apache.org/jira/browse/AVRO-1124) ticket.

The patch was originally submitted by Jay Kreps and later on substantially refactored by Scott Carey. Some other people then contributed minor fixes and improvements.

The schema repo was separated into a standalone project because it is unclear that Apache Avro is an appropriate parent project for containing it. It was given its own repository in order to ease further development. The whole project is Apache-licensed, so any OSS project can choose to use (or even include) the schema repo.
