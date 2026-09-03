#!/bin/bash

# End-to-end test for the MongoDB tooling:
#  1. Build a local release (docker image foilen/database-tools:<branch>-SNAPSHOT)
#  2. Start a local MongoDB docker instance
#  3. Generate a manage config from the running database (mongodb-create-manage)
#  4. Apply that config back using the freshly built docker image (mongodb-manage)

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# --- Config ---
export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
IMAGE=foilen/database-tools:$VERSION
INSTANCE=database-tools-test-mongodb
DB_PASSWORD=ABC
DOCKER_HOST_IP=172.17.0.1
HOST_PORT=47017
WORK_DIR=$RUN_PATH/build/test-mongodb

cleanup() {
    echo "----[ Cleanup ]----"
    docker rm -f $INSTANCE >/dev/null 2>&1 || true
}
trap cleanup EXIT

# --- 1. Local release ---
echo "----[ Create local release ]----"
./create-local-release-no-tests.sh

# --- 2. Start MongoDB ---
echo "----[ Start MongoDB ($INSTANCE) ]----"
docker rm -f $INSTANCE >/dev/null 2>&1 || true

docker run \
  --rm \
  --name $INSTANCE \
  -p $HOST_PORT:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=$DB_PASSWORD \
  -d mongo:7.0

echo "Waiting for MongoDB to be ready"
until docker exec -i $INSTANCE mongosh --quiet -u root -p $DB_PASSWORD --authenticationDatabase admin --eval 'db.runCommand({ ping: 1 })' >/dev/null 2>&1
do
  if ! docker ps --format '{{.Names}}' | grep -q "^$INSTANCE\$"; then
    echo "[ERROR] MongoDB container $INSTANCE is not running. Logs:"
    docker logs $INSTANCE 2>&1 | tail -20 || true
    exit 1
  fi
  sleep 5
done

echo "Seeding some objects"
docker exec -i $INSTANCE mongosh --quiet -u root -p $DB_PASSWORD --authenticationDatabase admin << _EOF
  db.getSiblingDB("yo").test.insertOne({ hello: "world" });
_EOF
echo "[DONE] MongoDB ready"

# --- 3. Generate config ---
echo "----[ Generate manage config ]----"
rm -rf $WORK_DIR
mkdir -p $WORK_DIR

cat > $WORK_DIR/login.json << _EOF
{
  "connection": {
    "jdbcUri": "jdbc:mongodb://root:$DB_PASSWORD@$DOCKER_HOST_IP:$HOST_PORT/"
  }
}
_EOF

USER_ID=$(id -u)
docker run -i \
  --rm \
  --user $USER_ID \
  --volume $WORK_DIR:/data \
  $IMAGE \
    mongodb-create-manage --connectionConfig /data/login.json --outputFile /data/config.json

echo "----[ Generated config.json ]----"
cat $WORK_DIR/config.json

# --- 4. Apply config ---
echo "----[ Apply manage config ]----"
docker run -i \
  --rm \
  --user $USER_ID \
  --volume $WORK_DIR:/data \
  $IMAGE \
    mongodb-manage --configFiles /data/config.json

echo "[SUCCESS] MongoDB end-to-end test completed"
