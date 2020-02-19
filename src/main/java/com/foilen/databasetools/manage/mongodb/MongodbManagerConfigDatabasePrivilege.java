/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MongodbManagerConfigDatabasePrivilege extends AbstractBasics implements Comparable<MongodbManagerConfigDatabasePrivilege> {

    private String database;
    private String collection;
    private List<String> actions = new ArrayList<>();

    public MongodbManagerConfigDatabasePrivilege() {
    }

    public MongodbManagerConfigDatabasePrivilege(String database, String collection, List<String> actions) {
        this.database = database;
        this.collection = collection;
        this.actions = actions;
    }

    @Override
    public int compareTo(MongodbManagerConfigDatabasePrivilege o) {
        return ComparisonChain.start() //
                .compare(database, o.database) //
                .compare(collection, o.collection) //
                .result();
    }

    public List<String> getActions() {
        return actions;
    }

    public String getCollection() {
        return collection;
    }

    public String getDatabase() {
        return database;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}
