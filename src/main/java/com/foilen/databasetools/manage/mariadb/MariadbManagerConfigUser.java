/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mariadb;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MariadbManagerConfigUser extends AbstractBasics implements Comparable<MariadbManagerConfigUser> {

    private String name;
    private String host;

    public MariadbManagerConfigUser() {
    }

    public MariadbManagerConfigUser(String name, String host) {
        this.name = name;
        this.host = host;
    }

    @Override
    public int compareTo(MariadbManagerConfigUser o) {
        return ComparisonChain.start() //
                .compare(this.name, o.name) //
                .compare(this.host, o.host) //
                .result();
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setName(String name) {
        this.name = name;
    }

}
