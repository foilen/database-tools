/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.connection;

import org.junit.Assert;
import org.junit.Test;

public class JdbcUriConfigConnectionTest {

    @Test
    public void testToString_EndsWithPassword() {
        Assert.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?password=*****&user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword").toString());
    }

    @Test
    public void testToString_WithoutPassword() {
        Assert.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root").toString());
    }

    @Test
    public void testToString_WithPassword() {
        Assert.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?connectTimeout=10000&password=*****&user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword&connectTimeout=10000").toString());
    }

    @Test
    public void testToString_WithPasswordInUserinfo() {
        Assert.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mongodb://root:*****@localhost/admin]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mongodb://root:ABC@localhost/admin").toString());
    }

}
