#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Prepare folder for docker image ]----
DOCKER_BUILD=$RUN_PATH/build/docker

rm -rf $DOCKER_BUILD
mkdir -p $DOCKER_BUILD/app

cp -v build/distributions/database-tools-$VERSION.zip $DOCKER_BUILD/app/database-tools.zip
cp -v docker-release/* $DOCKER_BUILD

cd $DOCKER_BUILD/app
unzip database-tools.zip
rm database-tools.zip
mv database-tools-$VERSION/* .
rm -rf database-tools-$VERSION

echo ----[ Docker image folder content ]----
find $DOCKER_BUILD

echo ----[ Build docker image ]----
DOCKER_IMAGE=database-tools:$VERSION
docker build -t $DOCKER_IMAGE $DOCKER_BUILD
docker tag $DOCKER_IMAGE foilen/$DOCKER_IMAGE

rm -rf $DOCKER_BUILD
