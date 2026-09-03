package com.foilen.databasetools.manage.mariadb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class MariadbManagerConfigDatabaseGrants extends AbstractBasics implements Comparable<MariadbManagerConfigDatabaseGrants> {

    private String databaseName;
    private List<String> grants = new ArrayList<>();

    public MariadbManagerConfigDatabaseGrants() {
    }

    public MariadbManagerConfigDatabaseGrants(String databaseName, String... grants) {
        this.databaseName = databaseName;
        this.grants.addAll(Arrays.asList(grants));
    }

    @Override
    public int compareTo(MariadbManagerConfigDatabaseGrants o) {
        return ComparisonChain.start() //
                .compare(this.databaseName, o.databaseName) //
                .result();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<String> getGrants() {
        return grants;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setGrants(List<String> grants) {
        this.grants = grants;
    }

}
