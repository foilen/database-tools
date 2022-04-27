/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.connection;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JdbcUriTools;

public class JdbcUriConfigConnection extends AbstractBasics {

    private String jdbcUri;

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
        builder.append("JdbcUriConfigConnection [jdbcUri=");

        if (jdbcUri == null) {
            builder.append(jdbcUri);
        } else {

            JdbcUriTools ju = new JdbcUriTools(jdbcUri);
            if (ju.getPassword() != null) {
                ju.setPassword("*****");
            }
            if (ju.getOptions().containsKey("password")) {
                ju.getOptions().put("password", "*****");
            }

            builder.append(ju.toUri());
        }

        builder.append("]");
        return builder.toString();
    }

}
