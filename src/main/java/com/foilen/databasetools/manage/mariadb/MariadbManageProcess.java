/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mariadb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.exception.RetryLaterException;
import com.foilen.databasetools.queries.MariadbQueries;
import com.foilen.smalltools.filesystemupdatewatcher.handler.OneFileUpdateNotifyer;
import com.foilen.smalltools.listscomparator.ListComparatorHandler;
import com.foilen.smalltools.listscomparator.ListsComparator;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;

public class MariadbManageProcess extends AbstractBasics implements Runnable {

    private String configFile;
    private boolean keepAlive;

    private AtomicBoolean process = new AtomicBoolean(true);

    public MariadbManageProcess(String configFile, boolean keepAlive) {
        this.configFile = configFile;
        this.keepAlive = keepAlive;
    }

    private void applyAllDatabases(MariadbQueries queries, List<String> databases) {

        if (databases == null) {
            logger.info("Databases not provided. Skipping");
            return;
        }

        logger.info("Apply all databases");

        logger.info("Get the current list of databases");
        List<String> currentDatabases = queries.databasesListNonSystem();
        Collections.sort(currentDatabases);
        logger.info("Current list size of databases: {}", currentDatabases.size());

        List<String> desiredDatabases = databases;
        Collections.sort(desiredDatabases);
        logger.info("Desired list size of databases: {}", desiredDatabases.size());

        ListsComparator.compareLists(currentDatabases, desiredDatabases, new ListComparatorHandler<String, String>() {

            @Override
            public void both(String left, String right) {
                logger.info("Database {} already exists. Skip", left);
            }

            @Override
            public void leftOnly(String current) {
                logger.info("Database {} exists, but is not desired. Delete", current);
                queries.databaseDelete(current);

            }

            @Override
            public void rightOnly(String desired) {
                logger.info("Database {} is missing. Create", desired);
                queries.databaseCreate(desired);
            }
        });
    }

    private void applyAllUsersAndGrants(MariadbQueries queries, List<MariadbManagerConfigUser> usersToIgnore, List<MariadbManagerConfigUserAndGrants> usersPermissions) {

        if (usersPermissions == null) {
            logger.info("Users and grants not provided. Skipping");
            return;
        }

        if (usersToIgnore == null) {
            usersToIgnore = new ArrayList<>();
        }

        logger.info("Apply all users and grants");
        AtomicBoolean hadChanges = new AtomicBoolean();

        logger.info("Get the current list of users");
        List<MariadbManagerConfigUserAndGrants> currentUsersAndGrants = queries.usersList();
        Collections.sort(currentUsersAndGrants);
        for (MariadbManagerConfigUser i : usersToIgnore) {
            currentUsersAndGrants.removeIf(u -> StringTools.safeEquals(u.getName(), i.getName()) && StringTools.safeEquals(u.getHost(), i.getHost()));
        }
        logger.info("Current list size of users (without ignored): {}", currentUsersAndGrants.size());

        List<MariadbManagerConfigUserAndGrants> desiredUsersAndGrants = usersPermissions;
        Collections.sort(desiredUsersAndGrants);
        for (MariadbManagerConfigUser i : usersToIgnore) {
            desiredUsersAndGrants.removeIf(u -> StringTools.safeEquals(u.getName(), i.getName()) && StringTools.safeEquals(u.getHost(), i.getHost()));
        }
        logger.info("Desired list size of users (without ignored): {}", desiredUsersAndGrants.size());

        // Create / drop users
        logger.info("Create / drop users");
        ListsComparator.compareStreams( //
                currentUsersAndGrants.stream().map(it -> it.toFullName()), //
                desiredUsersAndGrants.stream().map(it -> it.toFullName()), //
                new ListComparatorHandler<String, String>() {

                    @Override
                    public void both(String left, String right) {
                        logger.info("User {} already exists. Skip", left);
                    }

                    @Override
                    public void leftOnly(String current) {
                        logger.info("User {} exists, but is not desired. Delete", current);
                        queries.userDelete(current);
                        hadChanges.set(true);

                    }

                    @Override
                    public void rightOnly(String desired) {
                        logger.info("User {} is missing. Create", desired);
                        queries.userCreate(desired);
                        hadChanges.set(true);
                    }
                });

        // Update user details
        Map<String, MariadbManagerConfigUserAndGrants> currentUserByNameAndHost = currentUsersAndGrants.stream() //
                .collect(Collectors.toMap(u -> u.toFullName(), u -> u));
        desiredUsersAndGrants.forEach(desiredUser -> {

            String fullName = desiredUser.toFullName();
            logger.info("[{}] processing", fullName);

            // Get the current user
            MariadbManagerConfigUserAndGrants currentUser = currentUserByNameAndHost.get(fullName);
            if (currentUser == null) {
                currentUser = new MariadbManagerConfigUserAndGrants();
            }

            // Update password
            if (desiredUser.getHashedPassword() != null) {
                // Use hashed password
                if (!StringTools.safeEquals(desiredUser.getHashedPassword(), currentUser.getHashedPassword())) {
                    logger.info("[{}] hashed password is different. Updating", fullName);
                    queries.userPasswordUpdateHash(fullName, desiredUser.getHashedPassword());
                    hadChanges.set(true);
                }
            } else {
                if (desiredUser.getPassword() != null) {
                    logger.info("[{}] has a desired password. Updating", fullName);
                    queries.userPasswordUpdate(fullName, desiredUser.getPassword());
                    hadChanges.set(true);
                }
            }

            // Update global grants
            if (desiredUser.getGlobalGrants() == null) {
                logger.info("[{}] Global grants not provided. Skipping", fullName);
                return;
            }
            logger.info("[{}] Grant / revoke global grants", fullName);
            if (currentUser.getGlobalGrants() == null) {
                currentUser.setGlobalGrants(new ArrayList<>());
            }
            ListsComparator.compareStreams( //
                    currentUser.getGlobalGrants().stream().sorted().distinct(), //
                    desiredUser.getGlobalGrants().stream().sorted().distinct(), //
                    new ListComparatorHandler<String, String>() {

                        @Override
                        public void both(String left, String right) {
                        }

                        @Override
                        public void leftOnly(String current) {
                            logger.info("[{}] Global privilege {} is granted, but is not desired. Revoke", fullName, current);
                            queries.userPrivilegeGlobalRevoke(fullName, current);
                            hadChanges.set(true);
                        }

                        @Override
                        public void rightOnly(String desired) {
                            logger.info("[{}] Global privilege {} is missing. Grant it", fullName, desired);
                            queries.userPrivilegeGlobalGrant(fullName, desired);
                            hadChanges.set(true);
                        }
                    });

            // Update databaseGrants
            if (desiredUser.getGrantsByDatabase() == null) {
                logger.info("[{}] Databases grants not provided. Skipping", fullName);
                return;
            }
            logger.info("[{}] Grant / revoke databases grants", fullName);
            if (currentUser.getGrantsByDatabase() == null) {
                currentUser.setGrantsByDatabase(new HashMap<>());
            }
            ListsComparator.compareStreams( //
                    currentUser.getGrantsByDatabase().entrySet().stream().flatMap(e -> e.getValue().stream().map(v -> e.getKey() + "|" + v)).sorted().distinct(), //
                    desiredUser.getGrantsByDatabase().entrySet().stream().flatMap(e -> e.getValue().stream().map(v -> e.getKey() + "|" + v)).sorted().distinct(), //
                    new ListComparatorHandler<String, String>() {

                        @Override
                        public void both(String left, String right) {
                        }

                        @Override
                        public void leftOnly(String current) {
                            String[] parts = current.split("\\|");
                            String database = parts[0];
                            String privilege = parts[1];
                            logger.info("[{}] Database {} privilege {} is granted, but is not desired. Revoke", fullName, database, privilege);
                            queries.userPrivilegeDatabaseRevoke(fullName, database, privilege);
                            hadChanges.set(true);
                        }

                        @Override
                        public void rightOnly(String desired) {
                            String[] parts = desired.split("\\|");
                            String database = parts[0];
                            String privilege = parts[1];
                            logger.info("[{}] Database {} privilege {} is missing. Grant it", fullName, database, privilege);
                            queries.userPrivilegeDatabaseGrant(fullName, database, privilege);
                            hadChanges.set(true);
                        }
                    });

        });

        // If changes, flush
        if (hadChanges.get()) {
            queries.userPrivilegesFlush();
        }

    }

    private void execute() {

        try {
            // Load the config file
            MariadbManagerConfig mariadbManagerConfig;
            logger.info("Loading config file {}", configFile);
            mariadbManagerConfig = JsonTools.readFromFile(configFile, MariadbManagerConfig.class);

            // Get the connection
            JdbcUriConfigConnection connection = mariadbManagerConfig.getConnection();
            MariadbQueries queries = new MariadbQueries(connection);

            // Make the changes
            applyAllDatabases(queries, mariadbManagerConfig.getDatabases());
            applyAllUsersAndGrants(queries, mariadbManagerConfig.getUsersToIgnore(), mariadbManagerConfig.getUsersPermissions());
        } catch (CannotGetJdbcConnectionException e) {
            throw new RetryLaterException("Could not connect", 15000, e);
        }

    }

    @Override
    public void run() {

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .clear() //
                .setSeparator("-") //
                .appendText("Manage") //
                .appendText("MariaDB") //
                .appendText(configFile) //
                .change();
        try {

            // Check the config file changes when kept alive
            if (keepAlive) {
                logger.info("Start the file notifyer");
                File config = new File(configFile);
                @SuppressWarnings("resource")
                OneFileUpdateNotifyer oneFileUpdateNotifyer = new OneFileUpdateNotifyer(config.getAbsolutePath(), fileName -> {
                    logger.info("Config file changed. Update now");
                    process.set(true);
                });
                oneFileUpdateNotifyer.initAutoUpdateSystem();
            }

            long lastExecution = 0;
            boolean retry = true;
            while (retry || keepAlive) {
                retry = false;

                // Wait 1 hours if keep alive
                if (keepAlive) {
                    long nextExecutionOn = lastExecution + 60 * 60 * 1000;

                    if (!process.get()) {
                        long waitFor = nextExecutionOn - System.currentTimeMillis();
                        if (waitFor > 0) {
                            logger.info("Wait for {}ms before the next execution", waitFor);
                            while (waitFor > 0 && !process.get()) {
                                ThreadTools.sleep(Math.min(waitFor, 5000));
                                waitFor = nextExecutionOn - System.currentTimeMillis();
                            }
                            logger.info("End of wait");
                        }
                    }

                }

                // Execute
                try {
                    process.set(false);
                    lastExecution = System.currentTimeMillis();
                    execute();

                } catch (RetryLaterException e) {
                    logger.warn("Problem managing: {}. Will retry in {} ms", e.getMessage(), e.getRetryInMs());
                    retry = true;
                    process.set(true);
                    try {
                        Thread.sleep(e.getRetryInMs());
                    } catch (InterruptedException e1) {
                    }

                } catch (Exception e) {
                    logger.error("Problem managing", e);
                }
            }

        } finally {
            logger.info("End of manager");
            if (threadNameStateTool != null) {
                threadNameStateTool.revert();
            }
        }

    }

}
