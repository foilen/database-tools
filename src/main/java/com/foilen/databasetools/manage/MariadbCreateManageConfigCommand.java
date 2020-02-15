/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import com.foilen.databasetools.connection.MariadbConfigConnection;
import com.foilen.databasetools.manage.mariadb.MariadbManagerConfig;
import com.foilen.databasetools.queries.MariadbQueries;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.base.Strings;

public class MariadbCreateManageConfigCommand extends AbstractBasics implements Command<MariadbCreateManageConfigOptions> {

    @Override
    public void execute(MariadbCreateManageConfigOptions options) {
        MariadbManagerConfig databaseToConnectTo = JsonTools.readFromFile(options.getConnectionConfig(), MariadbManagerConfig.class);
        MariadbConfigConnection configConnection = databaseToConnectTo.getConnection();

        MariadbManagerConfig config = new MariadbManagerConfig();
        config.setConnection(configConnection);

        MariadbQueries queries = new MariadbQueries(configConnection);
        config.setDatabases(queries.databasesListNonSystem());
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
        return "mariadb-create-manage";
    }

    @Override
    public MariadbCreateManageConfigOptions newOptions() {
        return new MariadbCreateManageConfigOptions();
    }

}
