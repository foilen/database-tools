/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.foilen.databasetools.manage.Command;
import com.foilen.databasetools.manage.MariadbCreateManageConfigCommand;
import com.foilen.databasetools.manage.MariadbManageCommand;
import com.foilen.databasetools.manage.MongodbCreateManageConfigCommand;
import com.foilen.databasetools.manage.MongodbManageCommand;
import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ApplicationResourceUsageTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.LogbackTools;
import com.foilen.smalltools.tools.StringTools;

public class Application extends AbstractBasics {

    public static void main(String[] args) {
        new Application().execute(args);
    }

    private final List<Command<?>> commands = Arrays.asList( //
            new MariadbCreateManageConfigCommand(), //
            new MariadbManageCommand(), //
            new MongodbCreateManageConfigCommand(), //
            new MongodbManageCommand() //
    );

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void execute(String[] args) {

        Command command = null;
        Object options = null;

        try {

            List<String> arguments = new ArrayList<>();
            arguments.addAll(Arrays.asList(args));

            // Check for a log path
            System.setProperty("logDir", JavaEnvironmentValues.getWorkingDirectory());
            int logDirPos = arguments.indexOf("--logDir");
            if (logDirPos != -1) {
                if (arguments.size() >= logDirPos + 1) {
                    String logDir = arguments.get(logDirPos + 1);
                    System.out.println("Saving logs in " + logDir);
                    System.setProperty("logDir", logDir);
                    DirectoryTools.createPath(logDir);
                    arguments.remove(logDirPos);
                    arguments.remove(logDirPos);
                }
            }

            // Check which logger to use
            boolean debug = arguments.remove("--debug");
            if (debug) {
                System.out.println("Enabling LOGBACK debug");
                LogbackTools.changeConfig("/com/foilen/databasetools/logback-debug.xml");
            } else {
                System.out.println("Enabling LOGBACK normal");
                LogbackTools.changeConfig("/com/foilen/databasetools/logback.xml");
            }

            logger.info("Current user: {}", JavaEnvironmentValues.getUserName());

            // Check the command
            if (arguments.isEmpty()) {
                showUsage();
                return;
            }
            String commandName = arguments.remove(0);
            Optional<Command<?>> commandOptional = commands.stream().filter(c -> StringTools.safeEquals(commandName, c.getCommandName())).findFirst();
            if (!commandOptional.isPresent()) {
                showUsage();
                return;
            }
            command = commandOptional.get();

            // Check the command options
            options = command.newOptions();
            CmdLineParser cmdLineParser = new CmdLineParser(options);
            try {
                cmdLineParser.parseArgument(arguments);
            } catch (CmdLineException e) {
                e.printStackTrace();
                showUsage();
                return;
            }

            // Monitor the usage
            new ApplicationResourceUsageTools() //
                    .setDelayBetweenOutputInMs(60000) // 1 minute
                    .setShowThreadStackstrace(false) //
                    .setShowSystemMemory(false) //
                    .start();

        } catch (Exception e) {
            logger.error("Problem starting the application", e);
            System.exit(1);
        }

        // Launch the command
        command.execute(options);

    }

    private void showUsage() {
        System.out.println("Usage: <command> [options]");
        System.out.println("\noptions:");
        System.out.println("\t--logDir : Where to store logs (default: working directory");
        System.out.println("\t--debug : To show DEBUG level loggers");
        System.out.println("\ncommands:");
        commands.forEach(c -> {
            System.out.println("\t" + c.getCommandName());
            CmdLineParser cmdLineParser = new CmdLineParser(c.newOptions());
            cmdLineParser.printUsage(System.out);
        });

    }

}
