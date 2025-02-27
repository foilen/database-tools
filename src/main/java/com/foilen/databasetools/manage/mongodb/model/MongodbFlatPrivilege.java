/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

public class MongodbFlatPrivilege extends AbstractBasics implements Comparable<MongodbFlatPrivilege> {

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

    @Override
    public int compareTo(MongodbFlatPrivilege o) {

        int result = 0;

        // Cluster
        if (this.cluster == null) {
            if (o.cluster == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else if (o.cluster == null) {
            result = 1;
        } else if (this.cluster == o.cluster) {
            result = 0;
        } else {
            result = 1;
        }

        // database
        if (result == 0) {
            result = StringTools.safeComparisonNullFirst(this.database, o.database);
        }

        // collection
        if (result == 0) {
            result = StringTools.safeComparisonNullFirst(this.collection, o.collection);
        }

        return result;

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

    public Map<String, Object> toResource() {
        Map<String, Object> resource = new HashMap<String, Object>();
        if (database != null) {
            resource.put("db", database);
        }
        if (collection != null) {
            resource.put("collection", collection);
        }
        if (cluster != null) {
            resource.put("cluster", cluster);
        }
        return resource;
    }

    public String toResourceString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MongodbFlatPrivilege [database=");
        builder.append(database);
        builder.append(", collection=");
        builder.append(collection);
        builder.append(", cluster=");
        builder.append(cluster);
        builder.append("]");
        return builder.toString();
    }

}
