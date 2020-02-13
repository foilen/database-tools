/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.connection;

import com.foilen.smalltools.tools.AbstractBasics;

public class MariadbConfigConnection extends AbstractBasics {

    private String host = "localhost";
    private int port = 3306;
    private String username;
    private String password;

    public MariadbConfigConnection() {
    }

    public MariadbConfigConnection(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MariadbConfigConnection [host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        if (password != null) {
            builder.append("PROVIDED");
        } else {
            builder.append("NONE");
        }
        builder.append("]");
        return builder.toString();
    }

}
