/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

import com.foilen.smalltools.tools.AbstractBasics;

public class MariadbManageCommand extends AbstractBasics implements Command<MariadbManageOptions> {

    @Override
    public void execute(MariadbManageOptions options) {
        // TODO MariadbManageCommand
        System.out.println("TODO");

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
