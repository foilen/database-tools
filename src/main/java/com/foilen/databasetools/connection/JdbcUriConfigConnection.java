/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.connection;

import com.foilen.smalltools.tools.AbstractBasics;

public class JdbcUriConfigConnection extends AbstractBasics {

    private String jdbcUri = "jdbc:mysql://localhost:3306/";

    public JdbcUriConfigConnection() {
    }

    public String getJdbcUri() {
        return jdbcUri;
    }

    public JdbcUriConfigConnection setJdbcUri(String jdbcUri) {
        this.jdbcUri = jdbcUri;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MariadbConfigConnection [jdbcUri=");

        boolean asIs = false;
        if (jdbcUri == null) {
            asIs = true;
        } else {

            int passwordPos = jdbcUri.indexOf("password=");
            if (passwordPos == -1) {
                asIs = true;
            } else {
                int endPos = jdbcUri.indexOf("&", passwordPos);
                if (endPos == -1) {
                    endPos = jdbcUri.length();
                }
                String password = jdbcUri.substring(passwordPos + 9, endPos);
                builder.append(jdbcUri.replace(password, "*****"));
            }
        }

        if (asIs) {
            builder.append(jdbcUri);
        }

        builder.append("]");
        return builder.toString();
    }

}
