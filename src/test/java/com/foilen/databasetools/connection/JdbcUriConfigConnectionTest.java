/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.connection;

import org.junit.Assert;
import org.junit.Test;

public class JdbcUriConfigConnectionTest {

    @Test
    public void testToString_EndsWithPassword() {
        Assert.assertEquals("MariadbConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?user=root&password=*****]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword").toString());
    }

    @Test
    public void testToString_WithoutPassword() {
        Assert.assertEquals("MariadbConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root").toString());
    }

    @Test
    public void testToString_WithPassword() {
        Assert.assertEquals("MariadbConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?user=root&password=*****&connectTimeout=10000]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword&connectTimeout=10000").toString());
    }

}
