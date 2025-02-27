/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MongodbManagerConfigUser extends AbstractBasics implements Comparable<MongodbManagerConfigUser> {

    private String database;
    private String name;

    public MongodbManagerConfigUser() {
    }

    public MongodbManagerConfigUser(String database, String name) {
        this.database = database;
        this.name = name;
    }

    @Override
    public int compareTo(MongodbManagerConfigUser o) {
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

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setName(String name) {
        this.name = name;
    }

}
