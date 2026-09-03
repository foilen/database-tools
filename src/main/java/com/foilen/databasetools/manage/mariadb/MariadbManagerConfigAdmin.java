package com.foilen.databasetools.manage.mariadb;

import com.foilen.smalltools.tools.AbstractBasics;

public class MariadbManagerConfigAdmin extends AbstractBasics {

    private String name;
    private String password;

    public MariadbManagerConfigAdmin() {
    }

    public MariadbManagerConfigAdmin(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
