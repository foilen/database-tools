package com.foilen.databasetools.manage.mariadb;

import java.util.ArrayList;
import java.util.List;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.smalltools.tools.AbstractBasics;

public class MariadbManagerConfig extends AbstractBasics {

    private JdbcUriConfigConnection connection = new JdbcUriConfigConnection();
    private List<String> databases = new ArrayList<>();
    private List<MariadbManagerConfigUser> usersToIgnore = new ArrayList<>();
    private List<MariadbManagerConfigUserAndGrants> usersPermissions = new ArrayList<>();

    public JdbcUriConfigConnection getConnection() {
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

    public void setConnection(JdbcUriConfigConnection connection) {
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
