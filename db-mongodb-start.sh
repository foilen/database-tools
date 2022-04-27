#!/bin/bash

set -e

INSTANCE=mongo

PASS_FILE=$(mktemp)
echo -n ABC > $PASS_FILE

echo START MongoDB
docker run \
  --rm \
  --name $INSTANCE \
  -p 27017:27017 \
  -v $PASS_FILE:/newPass \
  -d foilen/fcloud-docker-mongodb:5.0.3-1 \
  /mongodb-start.sh
