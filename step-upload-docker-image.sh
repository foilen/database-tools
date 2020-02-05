#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Upload docker image ]----
DOCKER_IMAGE=database-tools:$VERSION
docker login
docker tag $DOCKER_IMAGE foilen/$DOCKER_IMAGE
docker tag $DOCKER_IMAGE foilen/database-tools:latest
docker push foilen/$DOCKER_IMAGE
docker push foilen/database-tools:latest
