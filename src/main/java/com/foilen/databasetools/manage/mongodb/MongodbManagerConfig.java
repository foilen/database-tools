/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatPrivilege;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatRole;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;

public class MongodbManagerConfig extends AbstractBasics {

    private JdbcUriConfigConnection connection = new JdbcUriConfigConnection();
    private List<String> databases = new ArrayList<>();
    private List<MongodbManagerConfigUser> usersToIgnore = new ArrayList<>();

    private Map<String, List<String>> globalClusterRoles = new HashMap<>();
    private Map<String, List<MongodbManagerConfigDatabasePrivilege>> globalDatabaseRoles = new HashMap<>();
    private Map<String, Map<String, List<MongodbManagerConfigCollectionPrivilege>>> roleByDatabase = new HashMap<>();

    private List<MongodbManagerConfigUserAndRoles> usersPermissions = new ArrayList<>();

    public JdbcUriConfigConnection getConnection() {
        return connection;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public Map<String, List<String>> getGlobalClusterRoles() {
        return globalClusterRoles;
    }

    public Map<String, List<MongodbManagerConfigDatabasePrivilege>> getGlobalDatabaseRoles() {
        return globalDatabaseRoles;
    }

    private MongodbFlatRole getOrCreateFlatRole(List<MongodbFlatRole> flatRoles, Map<String, MongodbFlatRole> flatRoleByFullName, String roleDatabase, String roleName) {
        String fullName = roleName + "@" + roleDatabase;
        MongodbFlatRole flatRole = flatRoleByFullName.get(fullName);
        if (flatRole == null) {
            flatRole = new MongodbFlatRole(roleDatabase, roleName);
            flatRoles.add(flatRole);
            flatRoleByFullName.put(fullName, flatRole);
        }
        return flatRole;
    }

    public Map<String, Map<String, List<MongodbManagerConfigCollectionPrivilege>>> getRoleByDatabase() {
        return roleByDatabase;
    }

    public List<MongodbManagerConfigUserAndRoles> getUsersPermissions() {
        return usersPermissions;
    }

    public List<MongodbManagerConfigUser> getUsersToIgnore() {
        return usersToIgnore;
    }

    public void loadRoles(List<MongodbFlatRole> flatRoles) {
        globalClusterRoles = new HashMap<>();
        globalDatabaseRoles = new HashMap<>();
        roleByDatabase = new HashMap<>();

        flatRoles.forEach(role -> {
            role.getPrivileges().forEach(privilege -> {
                if (Boolean.TRUE.equals(privilege.getCluster())) {
                    // Cluster
                    CollectionsTools.getOrCreateEmptyArrayList(globalClusterRoles, role.getRoleName(), String.class).addAll(privilege.getActions());
                } else {
                    // Database
                    boolean isInAdminDb = "admin".equals(role.getRoleDatabase());
                    if (isInAdminDb) {
                        CollectionsTools.getOrCreateEmptyArrayList(globalDatabaseRoles, role.getRoleName(), MongodbManagerConfigDatabasePrivilege.class)
                                .add(new MongodbManagerConfigDatabasePrivilege(privilege.getDatabase(), privilege.getCollection(), privilege.getActions()));
                    } else {

                        Map<String, List<MongodbManagerConfigCollectionPrivilege>> privilegesByRoleName = roleByDatabase.get(role.getRoleDatabase());
                        if (privilegesByRoleName == null) {
                            privilegesByRoleName = new HashMap<>();
                            roleByDatabase.put(role.getRoleDatabase(), privilegesByRoleName);
                        }
                        CollectionsTools.getOrCreateEmptyArrayList(privilegesByRoleName, role.getRoleName(), MongodbManagerConfigCollectionPrivilege.class)
                                .add(new MongodbManagerConfigCollectionPrivilege(privilege.getCollection(), privilege.getActions()));
                    }
                }
            });
        });

    }

    public void setConnection(JdbcUriConfigConnection connection) {
        this.connection = connection;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setGlobalClusterRoles(Map<String, List<String>> globalClusterRoles) {
        this.globalClusterRoles = globalClusterRoles;
    }

    public void setGlobalDatabaseRoles(Map<String, List<MongodbManagerConfigDatabasePrivilege>> globalDatabaseRoles) {
        this.globalDatabaseRoles = globalDatabaseRoles;
    }

    public void setRoleByDatabase(Map<String, Map<String, List<MongodbManagerConfigCollectionPrivilege>>> roleByDatabase) {
        this.roleByDatabase = roleByDatabase;
    }

    public void setUsersPermissions(List<MongodbManagerConfigUserAndRoles> usersPermissions) {
        this.usersPermissions = usersPermissions;
    }

    public void setUsersToIgnore(List<MongodbManagerConfigUser> usersToIgnore) {
        this.usersToIgnore = usersToIgnore;
    }

    public List<MongodbFlatRole> toFlatRoles() {

        List<MongodbFlatRole> flatRoles = new ArrayList<>();

        Map<String, MongodbFlatRole> flatRoleByFullName = new HashMap<>();

        // Cluster
        globalClusterRoles.forEach((roleName, actions) -> {
            MongodbFlatRole flatRole = getOrCreateFlatRole(flatRoles, flatRoleByFullName, "admin", roleName);
            flatRole.getPrivileges().add(new MongodbFlatPrivilege(null, null, true, actions));
        });

        // Admin database
        globalDatabaseRoles.forEach((roleName, databasePrivileges) -> {
            MongodbFlatRole flatRole = getOrCreateFlatRole(flatRoles, flatRoleByFullName, "admin", roleName);
            databasePrivileges.forEach(databasePrivilege -> {
                flatRole.getPrivileges().add(new MongodbFlatPrivilege(databasePrivilege.getDatabase(), databasePrivilege.getCollection(), null, databasePrivilege.getActions()));
            });
        });

        // By databases
        roleByDatabase.forEach((roleDatabase, collectionPrivilegesByRole) -> {
            collectionPrivilegesByRole.forEach((roleName, collectionPrivileges) -> {
                MongodbFlatRole flatRole = getOrCreateFlatRole(flatRoles, flatRoleByFullName, roleDatabase, roleName);
                collectionPrivileges.forEach(collectionPrivilege -> {
                    flatRole.getPrivileges().add(new MongodbFlatPrivilege(roleDatabase, collectionPrivilege.getCollection(), null, collectionPrivilege.getActions()));
                });
            });
        });

        Collections.sort(flatRoles);

        return flatRoles;
    }

}
