#!/bin/bash

cd `dirname "$0"`                                 # connect to root

function usage() {
	echo "Usage: $0 <backend-type>"
	echo ""
	echo "backend-type : Specify which backend to run. Valid values are: in-memory, file-system and zookeeper."
	exit 1
}

if [[ $# == 0 ]]; then
	usage
elif [[ $1 == 'in-memory' ]]; then
	echo "Starting Schema Repo Server with in In-Memory backend"
	java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/in-memory-config.properties
elif [[ $1 == 'file-system' ]]; then
	echo "Starting Schema Repo Server with local file system backend"
	java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/local-file-system-config.properties
elif [[ $1 == 'zookeeper' ]]; then
	echo "Starting Schema Repo Server with ZooKeeper backend"
	java -cp zk-bundle/target/schema-repo-zk-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer zk-bundle/config/config.properties
else
	usage
fi
