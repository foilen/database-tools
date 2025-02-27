/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the application.
 */
public class CommonCreateManageConfigOptions {

    @Option(name = "--connectionConfig", usage = "The config files that contains the connection information")
    private String connectionConfig;
    @Option(name = "--outputFile", usage = "The file where to store the configuration (default: output to shell)", required = false)
    private String outputFile;

    public String getConnectionConfig() {
        return connectionConfig;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setConnectionConfig(String connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

}
