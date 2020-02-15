/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.foilen.databasetools.manage.mariadb.MariadbManageProcess;
import com.foilen.databasetools.manage.mariadb.MariadbManagerConfig;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.JsonTools;

public class MariadbManageCommand extends AbstractBasics implements Command<MariadbManageOptions> {

    @Override
    public void execute(MariadbManageOptions options) {

        // Start all managers
        List<Future<?>> futures = new ArrayList<>();
        options.getConfigFiles().forEach(configFile -> {

            // Load the config file
            MariadbManagerConfig mariadbManagerConfig;
            try {
                logger.info("Loading config file {}", configFile);
                mariadbManagerConfig = JsonTools.readFromFile(configFile, MariadbManagerConfig.class);
            } catch (Exception e) {
                logger.error("Problem loading the config file {}", configFile, e);
                return;
            }

            // Start the process
            futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(new MariadbManageProcess(mariadbManagerConfig)));
        });

        // Wait for all managers to end
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
            }
        });
    }

    @Override
    public String getCommandName() {
        return "mariadb-manage";
    }

    @Override
    public MariadbManageOptions newOptions() {
        return new MariadbManageOptions();
    }

}
