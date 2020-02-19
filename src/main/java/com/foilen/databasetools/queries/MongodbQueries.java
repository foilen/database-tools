/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfigUserAndRoles;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatPrivilege;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatRole;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongodbQueries extends AbstractBasics {

    private static final Set<String> SYSTEM_DATABASES = new HashSet<>(Arrays.asList("admin", "config", "local"));

    private MongoClient mongoClient;

    public MongodbQueries(JdbcUriConfigConnection configConnection) {
        logger.info("Will use {}", configConnection);
        mongoClient = new MongoClient(new MongoClientURI(configConnection.getJdbcUri().replace("jdbc:", "")));
    }

    public void databaseCreate(String database) {
        logger.info("Create database {}", database);
        // TODO - Mongo databaseCreate
    }

    public void databaseDelete(String database) {
        logger.info("Delete database {}", database);
        // TODO - Mongo databaseDelete
    }

    public List<String> databasesListNonSystem() {
        return StreamSupport.stream(mongoClient.listDatabaseNames().spliterator(), false) //
                .filter(d -> !SYSTEM_DATABASES.contains(d)) //
                .collect(Collectors.toList());
    }

    public List<MongodbFlatRole> rolesList() {
        logger.info("Get roles list");
        List<MongodbFlatRole> flatRoles = new ArrayList<>();

        for (String databaseName : mongoClient.listDatabaseNames()) {
            flatRoles.addAll(rolesList(databaseName));
        }

        return flatRoles;

    }

    public List<MongodbFlatRole> rolesList(String databaseName) {
        logger.info("Get roles list for database {}", databaseName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        Document result = database.runCommand(new Document("rolesInfo", 1).append("showPrivileges", 1));

        List<MongodbFlatRole> flatRoles = new ArrayList<>();
        for (Document role : result.getList("roles", Document.class)) {
            logger.info("Found role {}", role);
            String roleName = role.getString("role");
            MongodbFlatRole flatRole = new MongodbFlatRole(databaseName, roleName);

            List<MongodbFlatPrivilege> flatPrivileges = new ArrayList<>();
            for (Document privilege : role.getList("privileges", Document.class)) {
                logger.info("Found privilege {}", privilege);

                Document resource = privilege.get("resource", Document.class);
                flatPrivileges.add(new MongodbFlatPrivilege( //
                        resource.getString("db"), resource.getString("collection"), //
                        resource.getBoolean("cluster"), //
                        privilege.getList("actions", String.class)));
            }

            flatRole.setPrivileges(flatPrivileges);
            flatRoles.add(flatRole);
        }

        return flatRoles;
    }

    public void userCreate(String user) {
        logger.info("Create user {}", user);
        // TODO - Mongo userCreate
    }

    public void userDelete(String user) {
        logger.info("Delete user {}", user);
        // TODO - Mongo userDelete
    }

    public void userPasswordUpdate(String user, String password) {
        logger.info("Update user password {}", user);
        // TODO - Mongo userPasswordUpdate
    }

    public void userPasswordUpdateHash(String user, String hashedPassword) {
        logger.info("Update user hashed password {}", user);
        // TODO - Mongo userPasswordUpdateHash
    }

    public void userPrivilegeDatabaseGrant(String user, String database, String privilege) {
        logger.info("Grant for user {} on database {} the privilege {}", user, database, privilege);
        // TODO - Mongo userPrivilegeDatabaseGrant
    }

    public void userPrivilegeDatabaseRevoke(String user, String database, String privilege) {
        logger.info("Revoke for user {} on database {} the privilege {}", user, database, privilege);
        // TODO - Mongo userPrivilegeDatabaseRevoke
    }

    public void userPrivilegeGlobalGrant(String user, String privilege) {
        logger.info("Grant for user {} globally the privilege {}", user, privilege);
        // TODO - Mongo userPrivilegeGlobalGrant
    }

    public void userPrivilegeGlobalRevoke(String user, String privilege) {
        logger.info("Revoke for user {} globally the privilege {}", user, privilege);
        // TODO - Mongo userPrivilegeGlobalRevoke
    }

    public List<MongodbManagerConfigUserAndRoles> usersList() {
        logger.info("Get users list");
        List<MongodbManagerConfigUserAndRoles> userAndRoles = new ArrayList<>();

        for (String databaseName : mongoClient.listDatabaseNames()) {
            userAndRoles.addAll(usersList(databaseName));
        }

        return userAndRoles;
    }

    public List<MongodbManagerConfigUserAndRoles> usersList(String databaseName) {
        logger.info("Get users list for database {}", databaseName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        Document result = database.runCommand(new Document("usersInfo", 1));

        List<MongodbManagerConfigUserAndRoles> usersAndRoles = new ArrayList<>();
        for (Document user : result.getList("users", Document.class)) {
            logger.info("Found user {}", user);
            String userName = user.getString("user");
            MongodbManagerConfigUserAndRoles userAndRoles = new MongodbManagerConfigUserAndRoles(databaseName, userName);
            usersAndRoles.add(userAndRoles);
            userAndRoles.setRolesByDatabase(new HashMap<String, List<String>>());
            for (Document role : user.getList("roles", Document.class)) {
                CollectionsTools.getOrCreateEmptyArrayList(userAndRoles.getRolesByDatabase(), role.getString("db"), String.class) //
                        .add(role.getString("role"));
            }
        }

        return usersAndRoles;
    }

}
