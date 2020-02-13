/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

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

import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

import com.foilen.databasetools.connection.MariadbConfigConnection;
import com.foilen.databasetools.manage.mariadb.MariadbManagerConfigUserAndGrants;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;

public class MariaDbQueries extends AbstractBasics {

    private static final Set<String> SYSTEM_DATABASES = new HashSet<>(Arrays.asList("information_schema", "mariadb", "mysql", "performance_schema", "sys"));

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public MariaDbQueries(MariadbConfigConnection configConnection) {
        logger.info("Will use {}", configConnection);
        try {
            MariaDbPoolDataSource dataSource = new MariaDbPoolDataSource(configConnection.getHost(), configConnection.getPort(), "mysql");
            dataSource.setUser(configConnection.getUsername());
            dataSource.setPassword(configConnection.getPassword());

            jdbcTemplate = new JdbcTemplate(dataSource);
            this.dataSource = dataSource;
        } catch (SQLException e) {
            throw new DataSourceLookupFailureException("Could not get the MariaDB datasource", e);
        }
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
                grantByColumnName.put(columnName, grantName);
            }
        }
        return grantByColumnName;
    }

    public List<String> listNonSystemDatabases() {
        return jdbcTemplate.queryForList("SHOW DATABASES", String.class).stream() //
                .filter(db -> !SYSTEM_DATABASES.contains(db)) //
                .sorted() //
                .collect(CollectionsTools.collectToArrayList());
    }

    public List<MariadbManagerConfigUserAndGrants> listUsers() {

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
                            user.setHashedPassword(rs.getString("password"));

                            // Global grants
                            processGrants(rs, grantByColumnName, grant -> user.getGlobalGrants().add(grant));

                            userAndGrantsByUser.put(user.getName() + "@" + user.getHost(), user);
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

                    MariadbManagerConfigUserAndGrants user = userAndGrantsByUser.get(username + "@" + userhost);
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

}
