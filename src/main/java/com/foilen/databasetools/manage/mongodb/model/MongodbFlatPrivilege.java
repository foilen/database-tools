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

public class MongodbFlatPrivilege {

    private String database;
    private String collection;
    private Boolean cluster;

    private List<String> actions = new ArrayList<>();

    public MongodbFlatPrivilege(String database, String collection, Boolean cluster, List<String> actions) {
        this.database = database;
        this.collection = collection;
        this.cluster = cluster;
        this.actions = actions;
    }

    public List<String> getActions() {
        return actions;
    }

    public Boolean getCluster() {
        return cluster;
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

    public void setCluster(Boolean cluster) {
        this.cluster = cluster;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}
