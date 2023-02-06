/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MongodbManagerConfigCollectionPrivilege extends AbstractBasics implements Comparable<MongodbManagerConfigCollectionPrivilege> {

    private String collection;
    private List<String> actions = new ArrayList<>();

    public MongodbManagerConfigCollectionPrivilege() {
    }

    public MongodbManagerConfigCollectionPrivilege(String collection, List<String> actions) {
        this.collection = collection;
        this.actions = actions;
    }

    @Override
    public int compareTo(MongodbManagerConfigCollectionPrivilege o) {
        return ComparisonChain.start() //
                .compare(collection, o.collection) //
                .result();
    }

    public List<String> getActions() {
        return actions;
    }

    public String getCollection() {
        return collection;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

}
