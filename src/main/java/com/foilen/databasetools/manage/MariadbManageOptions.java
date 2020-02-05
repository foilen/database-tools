/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the application.
 */
public class MariadbManageOptions {

    @Option(name = "--managerConfigFile", usage = "The config file path for the manager service")
    private String managerConfigFile;

    public String getManagerConfigFile() {
        return managerConfigFile;
    }

    public void setManagerConfigFile(String managerConfigFile) {
        this.managerConfigFile = managerConfigFile;
    }

}
