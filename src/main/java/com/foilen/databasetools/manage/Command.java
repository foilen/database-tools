/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage;

public interface Command<O> {

    /**
     * The command's code.
     *
     * @param options the options
     */
    void execute(O options);

    /**
     * The name of the command name to use.
     *
     * @return the command name
     */
    String getCommandName();

    /**
     * Get the arguments options object.
     *
     * @return the option objects
     */
    O newOptions();

}
