#!/bin/bash

cd `dirname "$0"`                                 # connect to root

if [[ $# == 0 ]]; then
	echo "Starting Schema Repo Server"
	java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/config.properties
elif [[ $1 == '--zk' ]]; then
	echo "Starting Schema Repo Server on ZK"
	java -cp zk-bundle/target/schema-repo-zk-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer zk-bundle/config/config.properties
else
	echo "Usage: $0 [--zk]"
	echo ""
	echo "--zk Use this flag to run the ZK-backed repo."
	exit 1
fi
