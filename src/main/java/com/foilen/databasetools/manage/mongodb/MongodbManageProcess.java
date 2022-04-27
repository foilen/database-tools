/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.exception.ProblemException;
import com.foilen.databasetools.manage.exception.RetryLaterException;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatPrivilege;
import com.foilen.databasetools.manage.mongodb.model.MongodbFlatRole;
import com.foilen.databasetools.queries.MongodbQueries;
import com.foilen.smalltools.filesystemupdatewatcher.handler.OneFileUpdateNotifyer;
import com.foilen.smalltools.listscomparator.ItemsComparator;
import com.foilen.smalltools.listscomparator.ListComparatorHandler;
import com.foilen.smalltools.listscomparator.ListsComparator;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.collect.ComparisonChain;
import com.mongodb.MongoNotPrimaryException;
import com.mongodb.MongoTimeoutException;

public class MongodbManageProcess extends AbstractBasics implements Runnable {

    private String configFile;
    private boolean keepAlive;

    private AtomicBoolean process = new AtomicBoolean(true);

    public MongodbManageProcess(String configFile, boolean keepAlive) {
        this.configFile = configFile;
        this.keepAlive = keepAlive;
    }

    private void applyAllDatabases(MongodbQueries queries, List<String> databases) {

        if (databases == null) {
            logger.info("Databases not provided. Skipping");
            return;
        }

        logger.info("Apply all databases (can only drop those not desired ; must create a collection to actually create a DB)");

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
                queries.databaseRemove(current);

            }

            @Override
            public void rightOnly(String desired) {
                logger.info("Database {} is missing. Skip", desired);
            }
        });
    }

    private void applyAllRoles(MongodbQueries queries, List<MongodbFlatRole> flatRoles) {

        if (flatRoles == null) {
            logger.info("Roles not provided. Skipping");
            return;
        }

        logger.info("Apply all roles");

        logger.info("Get the current list of roles");
        List<MongodbFlatRole> currentRoles = queries.rolesList();
        Collections.sort(currentRoles);
        logger.info("Current list size of roles: {}", currentRoles.size());

        List<MongodbFlatRole> desiredRoles = flatRoles;
        Collections.sort(desiredRoles);
        logger.info("Desired list size of roles: {}", desiredRoles.size());

        ListsComparator.compareLists(currentRoles, desiredRoles, new ListComparatorHandler<MongodbFlatRole, MongodbFlatRole>() {

            @Override
            public void both(MongodbFlatRole left, MongodbFlatRole right) {
                logger.info("Role {} already exists. Skip", left);
                applyRolePrivileges(queries, left, right);
            }

            @Override
            public void leftOnly(MongodbFlatRole current) {
                logger.info("Role {} exists, but is not desired. Delete", current);
                queries.roleRemove(current.getRoleDatabase(), current.getRoleName());
            }

            @Override
            public void rightOnly(MongodbFlatRole desired) {
                logger.info("Role {} is missing. Create", desired);
                queries.roleCreate(desired.getRoleDatabase(), desired.getRoleName());
                applyRolePrivileges(queries, new MongodbFlatRole(desired.getRoleDatabase(), desired.getRoleName()), desired);
            }
        });
    }

    private void applyAllUsersAndRoles(MongodbQueries queries, List<MongodbManagerConfigUser> usersToIgnore, List<MongodbManagerConfigUserAndRoles> usersPermissions) {

        if (usersPermissions == null) {
            logger.info("Users and roles not provided. Skipping");
            return;
        }

        if (usersToIgnore == null) {
            usersToIgnore = new ArrayList<>();
        }

        logger.info("Apply all users and roles");

        logger.info("Get the current list of users");
        List<MongodbManagerConfigUserAndRoles> currentUsersAndRoles = queries.usersList();
        Collections.sort(currentUsersAndRoles);
        for (MongodbManagerConfigUser i : usersToIgnore) {
            currentUsersAndRoles.removeIf(u -> StringTools.safeEquals(u.getDatabase(), i.getDatabase()) && StringTools.safeEquals(u.getName(), i.getName()));
        }
        logger.info("Current list size of users (without ignored): {}", currentUsersAndRoles.size());

        List<MongodbManagerConfigUserAndRoles> desiredUsersAndGrants = usersPermissions;
        Collections.sort(desiredUsersAndGrants);
        for (MongodbManagerConfigUser i : usersToIgnore) {
            desiredUsersAndGrants.removeIf(u -> StringTools.safeEquals(u.getDatabase(), i.getDatabase()) && StringTools.safeEquals(u.getName(), i.getName()));
        }
        logger.info("Desired list size of users (without ignored): {}", desiredUsersAndGrants.size());

        Map<String, MongodbManagerConfigUserAndRoles> currentUsersAndRolesByFullName = currentUsersAndRoles.stream().collect(Collectors.toMap(u -> u.toFullName(), u -> u));
        Map<String, MongodbManagerConfigUserAndRoles> desiredUsersAndRolesByFullName = desiredUsersAndGrants.stream().collect(Collectors.toMap(u -> u.toFullName(), u -> u));
        // Create / drop users
        logger.info("Create / drop users");
        ListsComparator.compareStreams( //
                currentUsersAndRoles.stream().map(it -> it.toFullName()), //
                desiredUsersAndGrants.stream().map(it -> it.toFullName()), //
                new ListComparatorHandler<String, String>() {

                    @Override
                    public void both(String current, String desired) {
                        logger.info("User {} already exists. Skip", current);

                        MongodbManagerConfigUserAndRoles currentUserAndRoles = currentUsersAndRolesByFullName.get(desired);
                        MongodbManagerConfigUserAndRoles desiredUserAndRoles = desiredUsersAndRolesByFullName.get(desired);

                        // Password
                        if (desiredUserAndRoles.getPassword() != null) {
                            queries.userPasswordUpdate(desiredUserAndRoles.getDatabase(), desiredUserAndRoles.getName(), desiredUserAndRoles.getPassword());
                        }

                        // Roles
                        applyUserRoles(queries, desiredUserAndRoles.getDatabase(), desiredUserAndRoles.getName(), currentUserAndRoles.getRolesByDatabase(), desiredUserAndRoles.getRolesByDatabase());
                    }

                    @Override
                    public void leftOnly(String current) {
                        logger.info("User {} exists, but is not desired. Delete", current);

                        MongodbManagerConfigUserAndRoles currentUserAndRoles = currentUsersAndRolesByFullName.get(current);
                        queries.userRemove(currentUserAndRoles.getDatabase(), currentUserAndRoles.getName());
                    }

                    @Override
                    public void rightOnly(String desired) {
                        logger.info("User {} is missing. Create", desired);

                        MongodbManagerConfigUserAndRoles desiredUserAndRoles = desiredUsersAndRolesByFullName.get(desired);
                        queries.userCreate(desiredUserAndRoles.getDatabase(), desiredUserAndRoles.getName(), desiredUserAndRoles.getPassword());

                        // Roles
                        applyUserRoles(queries, desiredUserAndRoles.getDatabase(), desiredUserAndRoles.getName(), new HashMap<>(), desiredUserAndRoles.getRolesByDatabase());
                    }
                });

    }

    private void applyRolePrivileges(MongodbQueries queries, MongodbFlatRole currentRole, MongodbFlatRole desiredRole) {

        if (desiredRole == null) {
            logger.info("Privileges for role {} / {} are not provided. Skipping", currentRole.getRoleDatabase(), currentRole.getRoleName());
            return;
        }

        logger.info("Apply role {} / {} privileges", desiredRole.getRoleDatabase(), desiredRole.getRoleName());

        ListsComparator.compareStreams( //
                currentRole.getPrivileges().stream().sorted(), //
                desiredRole.getPrivileges().stream().sorted(), //
                new ListComparatorHandler<MongodbFlatPrivilege, MongodbFlatPrivilege>() {

                    @Override
                    public void both(MongodbFlatPrivilege current, MongodbFlatPrivilege desired) {
                        logger.info("Role {} / {} already has privilege {}. Check actions", currentRole.getRoleDatabase(), currentRole.getRoleName(), desired.toResourceString());
                        Collections.sort(current.getActions());
                        Collections.sort(desired.getActions());
                        List<String> actionsToAdd = new ArrayList<>(desired.getActions());
                        actionsToAdd.removeAll(current.getActions());
                        List<String> actionsToRemove = new ArrayList<>(current.getActions());
                        actionsToRemove.removeAll(desired.getActions());

                        if (!actionsToAdd.isEmpty()) {
                            queries.rolePrivilegeAdd(currentRole.getRoleDatabase(), currentRole.getRoleName(), desired, actionsToAdd);
                        }
                        if (!actionsToRemove.isEmpty()) {
                            queries.rolePrivilegeRemove(currentRole.getRoleDatabase(), currentRole.getRoleName(), desired, actionsToRemove);
                        }
                    }

                    @Override
                    public void leftOnly(MongodbFlatPrivilege current) {
                        logger.info("Role {} / {} has privilege {} , but is not desired. Remove", currentRole.getRoleDatabase(), currentRole.getRoleName(), current.toResourceString());
                        queries.rolePrivilegeRemove(currentRole.getRoleDatabase(), currentRole.getRoleName(), current, current.getActions());
                    }

                    @Override
                    public void rightOnly(MongodbFlatPrivilege desired) {
                        logger.info("Role {} / {} does not have privilege {} and it is desired. Create and add actions", currentRole.getRoleDatabase(), currentRole.getRoleName(),
                                desired.toResourceString());
                        queries.rolePrivilegeAdd(currentRole.getRoleDatabase(), currentRole.getRoleName(), desired, desired.getActions());
                    }

                });

    }

    private void applyUserRoles(MongodbQueries queries, String database, String user, Map<String, List<String>> currentRolesByDatabase, Map<String, List<String>> desiredRolesByDatabase) {

        if (desiredRolesByDatabase == null) {
            logger.info("Roles for user {} / {} are not provided. Skipping", database, user);
            return;
        }

        logger.info("Apply user {} / {} roles by database", database, user);

        ListsComparator.compareStreams( //
                currentRolesByDatabase.entrySet().stream() //
                        .flatMap(e -> e.getValue().stream().map(v -> new Tuple2<>(e.getKey(), v))) //
                        .sorted((a, b) -> ComparisonChain.start().compare(a.getA(), b.getA()).compare(a.getB(), b.getB()).result()) //
                        .distinct(), //
                desiredRolesByDatabase.entrySet().stream()//
                        .flatMap(e -> e.getValue().stream().map(v -> new Tuple2<>(e.getKey(), v))) //
                        .sorted((a, b) -> ComparisonChain.start().compare(a.getA(), b.getA()).compare(a.getB(), b.getB()).result()) //
                        .distinct(), //
                new ItemsComparator<Tuple2<String, String>, Tuple2<String, String>>() {

                    @Override
                    public int compareTo(Tuple2<String, String> a, Tuple2<String, String> b) {
                        return ComparisonChain.start() //
                                .compare(a.getA(), b.getA()) //
                                .compare(a.getB(), b.getB()) //
                                .result();
                    }
                }, //
                new ListComparatorHandler<Tuple2<String, String>, Tuple2<String, String>>() {

                    @Override
                    public void both(Tuple2<String, String> current, Tuple2<String, String> desired) {
                        logger.info("User {} / {} already has role {} / {}. Skip", database, user, desired.getA(), desired.getB());

                    }

                    @Override
                    public void leftOnly(Tuple2<String, String> current) {
                        logger.info("User {} / {} has role {} / {} , but is not desired. Remove", database, user, current.getA(), current.getB());
                        queries.userRoleRevoke(database, user, current.getA(), current.getB());
                    }

                    @Override
                    public void rightOnly(Tuple2<String, String> desired) {
                        logger.info("User {} / {} does not have role {} / {} and it is desired. Grant", database, user, desired.getA(), desired.getB());
                        queries.userRoleGrant(database, user, desired.getA(), desired.getB());
                    }

                });

    }

    private void execute() {

        try {
            // Load the config file
            MongodbManagerConfig mongodbManagerConfig;
            logger.info("Loading config file {}", configFile);
            mongodbManagerConfig = JsonTools.readFromFile(configFile, MongodbManagerConfig.class);

            // Get the connection
            JdbcUriConfigConnection connection = mongodbManagerConfig.getConnection();
            MongodbQueries queries = new MongodbQueries(connection);

            // Make the changes
            applyAllDatabases(queries, mongodbManagerConfig.getDatabases());
            applyAllRoles(queries, mongodbManagerConfig.toFlatRoles());
            applyAllUsersAndRoles(queries, mongodbManagerConfig.getUsersToIgnore(), mongodbManagerConfig.getUsersPermissions());
        } catch (MongoTimeoutException | MongoNotPrimaryException e) {
            throw new RetryLaterException("Could not connect", 15000, e);
        }

    }

    @Override
    public void run() {

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .clear() //
                .setSeparator("-") //
                .appendText("Manage") //
                .appendText("MongoDB") //
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
                    logger.warn("Problem managing: {}. Will retry in {} seconds", e.getMessage(), e.getRetryInMs());
                    retry = true;
                    process.set(true);
                    try {
                        Thread.sleep(e.getRetryInMs());
                    } catch (InterruptedException e1) {
                    }

                } catch (Exception e) {
                    logger.error("Problem managing", e);
                    if (!keepAlive) {
                        throw new ProblemException("Killing the process");
                    }
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
