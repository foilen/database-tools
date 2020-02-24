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
import java.util.Collections;
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

    private static final String DB_ADMIN = "admin";

    private static final Set<String> SYSTEM_DATABASES = new HashSet<>(Arrays.asList(DB_ADMIN, "config", "local"));

    private MongoClient mongoClient;

    public MongodbQueries(JdbcUriConfigConnection configConnection) {
        logger.info("Will use {}", configConnection);
        mongoClient = new MongoClient(new MongoClientURI(configConnection.getJdbcUri().replace("jdbc:", "")));
    }

    public void databaseRemove(String database) {
        logger.info("[REMOVE] Database {}", database);

        mongoClient.getDatabase(database).drop();
    }

    public List<String> databasesListNonSystem() {
        return StreamSupport.stream(mongoClient.listDatabaseNames().spliterator(), false) //
                .filter(d -> !SYSTEM_DATABASES.contains(d)) //
                .collect(Collectors.toList());
    }

    public void roleCreate(String roleDatabase, String roleName) {
        logger.info("[CREATE] Role {} / {}", roleDatabase, roleName);

        mongoClient.getDatabase(roleDatabase).runCommand(new Document("createRole", roleName) //
                .append("roles", Collections.emptyList()) //
                .append("privileges", Collections.emptyList()) //
        );
    }

    public void roleRemove(String roleDatabase, String roleName) {
        logger.info("[REMOVE] Role {} / {}", roleDatabase, roleName);

        mongoClient.getDatabase(roleDatabase).runCommand(new Document("dropRole", roleName));
    }

    public void rolePrivilegeAdd(String roleDatabase, String roleName, MongodbFlatPrivilege privilege, List<String> actionsToAdd) {
        logger.info("[ADD] For role {} / {} add privilege {} actions {}", roleDatabase, roleName, privilege, actionsToAdd);

        mongoClient.getDatabase(roleDatabase).runCommand(new Document("grantPrivilegesToRole", roleName) //
                .append("privileges", Arrays.asList( //
                        new Document("resource", privilege.toResource()).append("actions", actionsToAdd) //
                ) //
                ) //
        );

    }

    public void rolePrivilegeRemove(String roleDatabase, String roleName, MongodbFlatPrivilege privilege, List<String> actionsToRemove) {
        logger.info("[REMOVE] For role {} / {} remove privilege {} actions {}", roleDatabase, roleName, privilege, actionsToRemove);

        mongoClient.getDatabase(roleDatabase).runCommand(new Document("revokePrivilegesFromRole", roleName) //
                .append("privileges", Arrays.asList( //
                        new Document("resource", privilege.toResource()).append("actions", actionsToRemove) //
                ) //
                ) //
        );
    }

    public List<MongodbFlatRole> rolesList() {
        logger.info("Get roles list");
        List<MongodbFlatRole> flatRoles = new ArrayList<>();

        // Find all roles
        for (String databaseName : mongoClient.getDatabase(DB_ADMIN).getCollection("system.roles").distinct("db", String.class)) {
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

    public void userCreate(String database, String user, String password) {
        logger.info("[CREATE] User {} / {}", database, user);

        mongoClient.getDatabase(database).runCommand(new Document("createUser", user).append("pwd", password).append("roles", Collections.emptyList()));
    }

    public void userRemove(String database, String user) {
        logger.info("[REMOVE] User {} / {}", database, user);

        mongoClient.getDatabase(database).runCommand(new Document("dropUser", user));
    }

    public void userPasswordUpdate(String database, String user, String password) {
        logger.info("[UPDATE] User password {} / {}", database, user);

        mongoClient.getDatabase(database).runCommand(new Document("updateUser", user).append("pwd", password));
    }

    public void userRoleGrant(String userDatabase, String userName, String roleDatabase, String roleName) {
        logger.info("[ADD] Grant user {} / {} role {} / {}", userDatabase, userName, roleDatabase, roleName);

        mongoClient.getDatabase(userDatabase).runCommand(new Document("grantRolesToUser", userName) //
                .append("roles", Arrays.asList( //
                        new Document("role", roleName).append("db", roleDatabase) //
                ) //
                ) //
        );
    }

    public void userRoleRevoke(String userDatabase, String userName, String roleDatabase, String roleName) {
        logger.info("[REMOVE] Revoke user {} / {} role {} / {}", userDatabase, userName, roleDatabase, roleName);

        mongoClient.getDatabase(userDatabase).runCommand(new Document("revokeRolesFromUser", userName) //
                .append("roles", Arrays.asList( //
                        new Document("role", roleName).append("db", roleDatabase) //
                ) //
                ) //
        );
    }

    public List<MongodbManagerConfigUserAndRoles> usersList() {
        logger.info("Get users list");
        List<MongodbManagerConfigUserAndRoles> userAndRoles = new ArrayList<>();

        for (String databaseName : mongoClient.getDatabase(DB_ADMIN).getCollection("system.users").distinct("db", String.class)) {
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
