/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import java.util.List;
import java.util.Map;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MongodbManagerConfigUserAndRoles extends AbstractBasics implements Comparable<MongodbManagerConfigUserAndRoles> {

    private String database;
    private String name;
    private String password;
    private Map<String, List<String>> rolesByDatabase;

    public MongodbManagerConfigUserAndRoles() {
    }

    public MongodbManagerConfigUserAndRoles(String database, String name) {
        this.database = database;
        this.name = name;
    }

    public MongodbManagerConfigUserAndRoles(String database, String name, String password) {
        this.database = database;
        this.name = name;
        this.password = password;
    }

    @Override
    public int compareTo(MongodbManagerConfigUserAndRoles o) {
        return ComparisonChain.start() //
                .compare(this.database, o.database) //
                .compare(this.name, o.name) //
                .result();
    }

    public String getDatabase() {
        return database;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, List<String>> getRolesByDatabase() {
        return rolesByDatabase;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRolesByDatabase(Map<String, List<String>> rolesByDatabase) {
        this.rolesByDatabase = rolesByDatabase;
    }

    public String toFullName() {
        return database + "." + name;
    }

}
