#!/bin/bash

if [[ $# != 1 ]]; then
	echo "Usage: $0 <new-version-number>"
	exit 1
else
	export VERSION=$1
	echo "Setting version to $VERSION"
	mvn versions:set -DnewVersion=$VERSION
fi
