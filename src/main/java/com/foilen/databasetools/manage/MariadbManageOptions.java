/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the application.
 */
public class MariadbManageOptions {

    @Option(name = "--keepAlive", usage = "Keep the manager alive and update when the file is updated", required = false)
    private boolean keepAlive = false;

    @Option(name = "--configFiles", usage = "The config files of the different databases to manage")
    private List<String> configFiles = new ArrayList<String>();

    public List<String> getConfigFiles() {
        return configFiles;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setConfigFiles(List<String> configFiles) {
        this.configFiles = configFiles;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

}
