/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mariadb;

import java.util.ArrayList;
import java.util.List;

import com.foilen.databasetools.connection.MariadbConfigConnection;
import com.foilen.smalltools.tools.AbstractBasics;

public class MariadbManagerConfig extends AbstractBasics {

    private MariadbConfigConnection connection = new MariadbConfigConnection();
    private List<String> databases = new ArrayList<>();
    private List<MariadbManagerConfigUser> usersToIgnore = new ArrayList<>();
    private List<MariadbManagerConfigUserAndGrants> usersPermissions = new ArrayList<>();

    public MariadbConfigConnection getConnection() {
        return connection;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<MariadbManagerConfigUserAndGrants> getUsersPermissions() {
        return usersPermissions;
    }

    public List<MariadbManagerConfigUser> getUsersToIgnore() {
        return usersToIgnore;
    }

    public void setConnection(MariadbConfigConnection connection) {
        this.connection = connection;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setUsersPermissions(List<MariadbManagerConfigUserAndGrants> usersPermissions) {
        this.usersPermissions = usersPermissions;
    }

    public void setUsersToIgnore(List<MariadbManagerConfigUser> usersToIgnore) {
        this.usersToIgnore = usersToIgnore;
    }

}
