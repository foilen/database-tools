/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb.model;

import java.util.ArrayList;
import java.util.List;

public class MongodbFlatRole {

    private String roleDatabase;
    private String roleName;

    private List<MongodbFlatPrivilege> privileges = new ArrayList<>();

    public MongodbFlatRole(String roleDatabase, String roleName) {
        this.roleDatabase = roleDatabase;
        this.roleName = roleName;
    }

    public List<MongodbFlatPrivilege> getPrivileges() {
        return privileges;
    }

    public String getRoleDatabase() {
        return roleDatabase;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setPrivileges(List<MongodbFlatPrivilege> privileges) {
        this.privileges = privileges;
    }

    public void setRoleDatabase(String roleDatabase) {
        this.roleDatabase = roleDatabase;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
