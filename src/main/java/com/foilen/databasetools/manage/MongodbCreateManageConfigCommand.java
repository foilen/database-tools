/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import com.foilen.databasetools.connection.JdbcUriConfigConnection;
import com.foilen.databasetools.manage.mongodb.MongodbManagerConfig;
import com.foilen.databasetools.queries.MongodbQueries;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.base.Strings;

public class MongodbCreateManageConfigCommand extends AbstractBasics implements Command<CommonCreateManageConfigOptions> {

    @Override
    public void execute(CommonCreateManageConfigOptions options) {
        MongodbManagerConfig databaseToConnectTo = JsonTools.readFromFile(options.getConnectionConfig(), MongodbManagerConfig.class);
        JdbcUriConfigConnection configConnection = databaseToConnectTo.getConnection();
        MongodbManagerConfig config = new MongodbManagerConfig();
        config.setConnection(configConnection);

        MongodbQueries queries = new MongodbQueries(configConnection);
        config.setDatabases(queries.databasesListNonSystem());
        config.loadRoles(queries.rolesList());

        config.setUsersPermissions(queries.usersList());

        if (Strings.isNullOrEmpty(options.getOutputFile())) {
            System.out.println(JsonTools.prettyPrint(config));
        } else {
            logger.info("Output config to {}", options.getOutputFile());
            JsonTools.writeToFile(options.getOutputFile(), config);
        }
    }

    @Override
    public String getCommandName() {
        return "mongodb-create-manage";
    }

    @Override
    public CommonCreateManageConfigOptions newOptions() {
        return new CommonCreateManageConfigOptions();
    }

}
