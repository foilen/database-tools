package com.foilen.databasetools.connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JdbcUriConfigConnectionTest {

    @Test
    public void testToString_EndsWithPassword() {
        Assertions.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?password=*****&user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword").toString());
    }

    @Test
    public void testToString_WithoutPassword() {
        Assertions.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root").toString());
    }

    @Test
    public void testToString_WithPassword() {
        Assertions.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mariadb://localhost:3306/DB?connectTimeout=10000&password=*****&user=root]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword&connectTimeout=10000").toString());
    }

    @Test
    public void testToString_WithPasswordInUserinfo() {
        Assertions.assertEquals("JdbcUriConfigConnection [jdbcUri=jdbc:mongodb://root:*****@localhost/admin]",
                new JdbcUriConfigConnection().setJdbcUri("jdbc:mongodb://root:ABC@localhost/admin").toString());
    }

}
