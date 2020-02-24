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

import com.foilen.databasetools.manage.mongodb.MongodbManageProcess;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ExecutorsTools;

public class MongodbManageCommand extends AbstractBasics implements Command<CommonManageOptions> {

    @Override
    public void execute(CommonManageOptions options) {

        // Start all managers
        List<Future<?>> futures = new ArrayList<>();
        options.getConfigFiles().forEach(configFile -> {
            futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(new MongodbManageProcess(configFile, options.isKeepAlive())));
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
        return "mongodb-manage";
    }

    @Override
    public CommonManageOptions newOptions() {
        return new CommonManageOptions();
    }

}
