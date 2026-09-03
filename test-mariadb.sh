#!/bin/bash

# End-to-end test for the MariaDB tooling:
#  1. Build a local release (docker image foilen/database-tools:<branch>-SNAPSHOT)
#  2. Start a local MariaDB docker instance
#  3. Generate a manage config from the running database (mariadb-create-manage)
#  4. Apply that config back using the freshly built docker image (mariadb-manage)

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# --- Config ---
export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
IMAGE=foilen/database-tools:$VERSION
INSTANCE=database-tools-test-mariadb
DB_PASSWORD=ABC
DOCKER_HOST_IP=172.17.0.1
HOST_PORT=13306
WORK_DIR=$RUN_PATH/build/test-mariadb

cleanup() {
    echo "----[ Cleanup ]----"
    docker rm -f $INSTANCE >/dev/null 2>&1 || true
}
trap cleanup EXIT

# --- 1. Local release ---
echo "----[ Create local release ]----"
./create-local-release-no-tests.sh

# --- 2. Start MariaDB ---
echo "----[ Start MariaDB ($INSTANCE) ]----"
docker rm -f $INSTANCE >/dev/null 2>&1 || true
docker run \
  --rm \
  --name $INSTANCE \
  -e MYSQL_ROOT_PASSWORD=$DB_PASSWORD \
  -p $HOST_PORT:3306 \
  -d mariadb:latest

echo "Waiting for MariaDB to be ready and seeding some objects (might fail a couple times)"
tries=0
until docker exec -i $INSTANCE mariadb -uroot -p$DB_PASSWORD << _EOF
  CREATE DATABASE IF NOT EXISTS potato;
  CREATE USER IF NOT EXISTS 'potato_user'@'%' IDENTIFIED BY 'secret';
  GRANT ALL ON potato.* TO 'potato_user'@'%';
_EOF
do
  tries=$((tries + 1))
  if [ $tries -ge 24 ]; then
    echo "[ERROR] MariaDB did not become ready in time"
    docker logs $INSTANCE || true
    exit 1
  fi
  sleep 5
done
echo "[DONE] MariaDB ready"

# --- 3. Generate config ---
echo "----[ Generate manage config ]----"
rm -rf $WORK_DIR
mkdir -p $WORK_DIR

cat > $WORK_DIR/login.json << _EOF
{
  "connection": {
    "jdbcUri": "jdbc:mariadb://$DOCKER_HOST_IP:$HOST_PORT/mysql?user=root&password=$DB_PASSWORD"
  }
}
_EOF

USER_ID=$(id -u)
docker run -i \
  --rm \
  --user $USER_ID \
  --volume $WORK_DIR:/data \
  $IMAGE \
    mariadb-create-manage --connectionConfig /data/login.json --outputFile /data/config.json

echo "----[ Generated config.json ]----"
cat $WORK_DIR/config.json

# --- 4. Apply config ---
echo "----[ Apply manage config ]----"
docker run -i \
  --rm \
  --user $USER_ID \
  --volume $WORK_DIR:/data \
  $IMAGE \
    mariadb-manage --configFiles /data/config.json

echo "[SUCCESS] MariaDB end-to-end test completed"
