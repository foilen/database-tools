/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mariadb;

import java.util.List;
import java.util.Map;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MariadbManagerConfigUserAndGrants extends AbstractBasics implements Comparable<MariadbManagerConfigUserAndGrants> {

    private String name;
    private String host;
    private String password;
    private String hashedPassword;
    private List<String> globalGrants;
    private Map<String, List<String>> grantsByDatabase;

    public MariadbManagerConfigUserAndGrants() {
    }

    public MariadbManagerConfigUserAndGrants(String name, String host) {
        this.name = name;
        this.host = host;
    }

    public MariadbManagerConfigUserAndGrants(String name, String host, String password) {
        this.name = name;
        this.host = host;
        this.password = password;
    }

    @Override
    public int compareTo(MariadbManagerConfigUserAndGrants o) {
        return ComparisonChain.start() //
                .compare(this.name, o.name) //
                .compare(this.host, o.host) //
                .result();
    }

    public List<String> getGlobalGrants() {
        return globalGrants;
    }

    public Map<String, List<String>> getGrantsByDatabase() {
        return grantsByDatabase;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setGlobalGrants(List<String> globalGrants) {
        this.globalGrants = globalGrants;
    }

    public void setGrantsByDatabase(Map<String, List<String>> grantsByDatabase) {
        this.grantsByDatabase = grantsByDatabase;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toFullName() {
        return "'" + name + "'@'" + host + "'";
    }

}
