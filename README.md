# About

This is an application to help manipulating databases.

# MariaDB

## Create a manage configuration

```
# Create the connection details file
cat > login.json << _EOF
{
  "connection": {
    "jdbcUri": "jdbc:mariadb://172.17.0.1:3306/mysql?user=root&password=ABC"
  }
}
_EOF

# Execute
USER_ID=$(id -u)
docker run -ti \
  --rm \
  --user $USER_ID \
  --volume $PWD:/data \
  foilen/database-tools \
    mariadb-create-manage --connectionConfig /data/login.json --outputFile /data/out.json
    
cat out.json
```

## Manage the database

```
# Create the configuration details file
cat > config.json << _EOF
{
  "connection": {
    "jdbcUri": "jdbc:mariadb://172.17.0.1:3306/mysql?user=root&password=ABC"
  },
  "databases" : [ "potato" ],
  "usersToIgnore" : [ {
    "name" : "root",
    "host" : "%"
    } , {
    "name" : "root",
    "host" : "localhost"
    } ],
  "usersPermissions" : [ {
    "name" : "aUser",
    "host" : "%",
    "password" : null,
    "hashedPassword" : "*14E65567ABDB5135D0CFD9A70B3032C179A49EE7",
    "globalGrants" : [ "CREATE ROUTINE", "ALTER", "REFERENCES", "INDEX", "PROCESS", "REPL SLAVE", "TRIGGER", "FILE", "DROP", "SELECT", "CREATE", "CREATE TABLESPACE", "EXECUTE", "SHOW VIEW", "INSERT", "CREATE TEMPORARY TABLES", "CREATE VIEW", "ALTER ROUTINE", "REPL CLIENT", "LOCK TABLES", "SHUTDOWN", "EVENT", "DELETE", "DELETE HISTORY", "SUPER", "RELOAD", "UPDATE", "CREATE USER", "SHOW DB" ],
    "grantsByDatabase" : { }
  }, {
    "name" : "potato_user",
    "host" : "%",
    "password" : null,
    "hashedPassword" : "*AA1420F182E88B9E5F874F6FBE7459291E8F4601",
    "globalGrants" : [ "CREATE" ],
    "grantsByDatabase" : {
      "potato" : [ "CREATE ROUTINE", "ALTER", "REFERENCES", "INDEX", "TRIGGER", "EVENT", "DELETE", "DROP", "SELECT", "CREATE", "SHOW VIEW", "EXECUTE", "DELETE HISTORY", "INSERT", "CREATE TEMPORARY TABLES", "CREATE VIEW", "ALTER ROUTINE", "LOCK TABLES", "UPDATE" ]
    }
  } ]
}
_EOF

# Execute
USER_ID=$(id -u)
docker run -ti \
  --rm \
  --user $USER_ID \
  --volume $PWD:/data \
  foilen/database-tools \
    mariadb-manage --configFiles /data/config.json --keepAlive
```
