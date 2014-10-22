#!/bin/bash

cd `dirname "$0"`                                 # connect to root

if [[ $# == 0 ]]; then
	echo "Starting Schema Repo Server"
	java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/config.properties
else
	echo "Usage: $0"
	exit 1
fi
