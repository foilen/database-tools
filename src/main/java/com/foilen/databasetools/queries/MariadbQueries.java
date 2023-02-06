/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.mariadb.MariadbManagerConfigUserAndGrants;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;

public class MariadbQueries extends AbstractBasics {

    private static final Set<String> SYSTEM_DATABASES = new HashSet<>(Arrays.asList("information_schema", "mariadb", "mysql", "performance_schema", "sys"));
    private static final Map<String, String> GRANT_BY_BAD_GRANT = new HashMap<String, String>();

    static {
        GRANT_BY_BAD_GRANT.put("CREATE TMP TABLE", "CREATE TEMPORARY TABLES");
    }

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public MariadbQueries(JdbcUriConfigConnection configConnection) {
        logger.info("Will use {}", configConnection);
        DriverManagerDataSource dataSource = new DriverManagerDataSource(configConnection.getJdbcUri());

        jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public void databaseCreate(String database) {
        logger.info("Create database {}", database);
        jdbcTemplate.update("CREATE DATABASE " + database);
    }

    public void databaseDelete(String database) {
        logger.info("Delete database {}", database);
        jdbcTemplate.update("DROP DATABASE " + database);
    }

    public List<String> databasesListNonSystem() {
        return jdbcTemplate.queryForList("SHOW DATABASES", String.class).stream() //
                .filter(db -> !SYSTEM_DATABASES.contains(db)) //
                .sorted() //
                .collect(CollectionsTools.collectToArrayList());
    }

    private Map<String, String> getGrantByColumnName(ResultSet rs) throws SQLException {
        Map<String, String> grantByColumnName = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            String columnName = metaData.getColumnName(i);
            if (columnName.toUpperCase().endsWith("_PRIV")) {
                String grantName = columnName.toUpperCase();
                grantName = grantName.substring(0, grantName.length() - 5);
                grantName = grantName.replaceAll("_", " ");
                if (GRANT_BY_BAD_GRANT.containsKey(grantName)) {
                    grantName = GRANT_BY_BAD_GRANT.get(grantName);
                }
                grantByColumnName.put(columnName, grantName);
            }
        }
        return grantByColumnName;
    }

    private void processGrants(ResultSet rs, Map<String, String> grantByColumnName, Consumer<String> consumer) throws SQLException {
        for (Entry<String, String> entry : grantByColumnName.entrySet()) {
            String columnName = entry.getKey();
            String grant = entry.getValue();

            String yesNo = rs.getString(columnName);
            if ("Y".equals(yesNo)) {
                consumer.accept(grant);
            }
        }
    }

    public void userCreate(String user) {
        logger.info("Create user {}", user);
        jdbcTemplate.update("CREATE USER " + user);
    }

    public void userDelete(String user) {
        logger.info("Delete user {}", user);
        jdbcTemplate.update("DROP USER " + user);
    }

    public void userPasswordUpdate(String user, String password) {
        logger.info("Update user password {}", user);
        jdbcTemplate.update("ALTER USER " + user + "IDENTIFIED BY '" + password + "'");
    }

    public void userPasswordUpdateHash(String user, String hashedPassword) {
        logger.info("Update user hashed password {}", user);
        jdbcTemplate.update("ALTER USER " + user + "IDENTIFIED BY PASSWORD '" + hashedPassword + "'");
    }

    public void userPrivilegeDatabaseGrant(String user, String database, String privilege) {
        logger.info("Grant for user {} on database {} the privilege {}", user, database, privilege);
        jdbcTemplate.update("GRANT " + privilege + " ON `" + database + "`.* TO " + user);
    }

    public void userPrivilegeDatabaseRevoke(String user, String database, String privilege) {
        logger.info("Revoke for user {} on database {} the privilege {}", user, database, privilege);
        jdbcTemplate.update("REVOKE " + privilege + " ON `" + database + "`.* FROM " + user);
    }

    public void userPrivilegeGlobalGrant(String user, String privilege) {
        logger.info("Grant for user {} globally the privilege {}", user, privilege);
        jdbcTemplate.update("GRANT " + privilege + " ON *.* TO " + user);
    }

    public void userPrivilegeGlobalRevoke(String user, String privilege) {
        logger.info("Revoke for user {} globally the privilege {}", user, privilege);
        jdbcTemplate.update("REVOKE " + privilege + " ON *.* FROM " + user);

    }

    public void userPrivilegesFlush() {
        logger.info("Flush privileges");
        jdbcTemplate.update("FLUSH PRIVILEGES");
    }

    public List<MariadbManagerConfigUserAndGrants> usersList() {

        try (Connection connection = dataSource.getConnection()) {
        } catch (Exception e) {
            // throw new
        }

        Map<String, MariadbManagerConfigUserAndGrants> userAndGrantsByUser = jdbcTemplate.query("SELECT * FROM user ORDER BY user, host",
                new ResultSetExtractor<Map<String, MariadbManagerConfigUserAndGrants>>() {
                    @Override
                    public Map<String, MariadbManagerConfigUserAndGrants> extractData(ResultSet rs) throws SQLException, DataAccessException {

                        // Get the column names
                        Map<String, String> grantByColumnName = getGrantByColumnName(rs);

                        // Go through all
                        Map<String, MariadbManagerConfigUserAndGrants> userAndGrantsByUser = new HashMap<>();
                        while (rs.next()) {

                            MariadbManagerConfigUserAndGrants user = new MariadbManagerConfigUserAndGrants(rs.getString("user"), rs.getString("host"));
                            user.setGlobalGrants(new ArrayList<>());
                            user.setGrantsByDatabase(new HashMap<>());
                            user.setHashedPassword(rs.getString("password"));

                            // Global grants
                            processGrants(rs, grantByColumnName, grant -> user.getGlobalGrants().add(grant));

                            userAndGrantsByUser.put(user.toFullName(), user);
                        }

                        return userAndGrantsByUser;
                    }

                });

        // Databases grants
        jdbcTemplate.query("SELECT * FROM db ORDER BY user, host, db", new ResultSetExtractor<Void>() {
            @Override
            public Void extractData(ResultSet rs) throws SQLException, DataAccessException {

                // Get the column names
                Map<String, String> grantByColumnName = getGrantByColumnName(rs);

                // Go through all
                while (rs.next()) {

                    String username = rs.getString("user");
                    String userhost = rs.getString("host");
                    String database = rs.getString("db");
                    String fullName = new MariadbManagerConfigUserAndGrants(username, userhost).toFullName();

                    MariadbManagerConfigUserAndGrants user = userAndGrantsByUser.get(fullName);
                    if (user == null) {
                        logger.warn("The user {}@{} has privilege on database {}, but that user does not exist", username, userhost, database);
                        continue;
                    }

                    // Specific grants
                    List<String> grants = new ArrayList<>();
                    processGrants(rs, grantByColumnName, grant -> grants.add(grant));
                    user.getGrantsByDatabase().put(database, grants);

                }

                return null;

            }

        });

        return userAndGrantsByUser.values().stream().sorted().collect(Collectors.toList());
    }

}
