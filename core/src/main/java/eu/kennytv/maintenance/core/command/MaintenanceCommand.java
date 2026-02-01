/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.subcommand.AbortTimerCommand;
import eu.kennytv.maintenance.core.command.subcommand.DebugCommand;
import eu.kennytv.maintenance.core.command.subcommand.DumpCommand;
import eu.kennytv.maintenance.core.command.subcommand.EndtimerCommand;
import eu.kennytv.maintenance.core.command.subcommand.HelpCommand;
import eu.kennytv.maintenance.core.command.subcommand.MotdCommand;
import eu.kennytv.maintenance.core.command.subcommand.ReloadCommand;
import eu.kennytv.maintenance.core.command.subcommand.RemoveMotdCommand;
import eu.kennytv.maintenance.core.command.subcommand.ScheduleTimerCommand;
import eu.kennytv.maintenance.core.command.subcommand.SetMotdCommand;
import eu.kennytv.maintenance.core.command.subcommand.StarttimerCommand;
import eu.kennytv.maintenance.core.command.subcommand.ToggleCommand;
import eu.kennytv.maintenance.core.command.subcommand.UpdateCommand;
import eu.kennytv.maintenance.core.command.subcommand.WhitelistAddCommand;
import eu.kennytv.maintenance.core.command.subcommand.WhitelistCommand;
import eu.kennytv.maintenance.core.command.subcommand.WhitelistRemoveCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;

public abstract class MaintenanceCommand {
    private static final long MAX_TASK_DURATION_SECONDS = TimeUnit.DAYS.toSeconds(28);
    protected final MaintenancePlugin plugin;
    protected final Settings settings;
    private final Map<String, CommandInfo> commandExecutors = new LinkedHashMap<>();
    private final List<CommandInfo> commands = new ArrayList<>();
    private final HelpCommand help;

    protected MaintenanceCommand(final MaintenancePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        help = new HelpCommand(plugin);
    }

    protected void registerCommands() {
        add(help, "help");
        add(new ReloadCommand(plugin), "reload");

        addToggleAndTimerCommands();

        add(new WhitelistCommand(plugin), "whitelist");
        add(new WhitelistAddCommand(plugin), "add");
        add(new WhitelistRemoveCommand(plugin), "remove");

        add(new SetMotdCommand(plugin), "setmotd");
        add(new RemoveMotdCommand(plugin), "removemotd");
        add(new MotdCommand(plugin), "motd");

        add(new UpdateCommand(plugin), "update", "forceupdate");
        add(new DumpCommand(plugin), "dump");
        add(new DebugCommand(plugin), "debug");
    }

    protected void add(final CommandInfo command, final String... aliases) {
        commands.add(command);
        for (final String alias : aliases) {
            commandExecutors.put(alias, command);
        }
    }

    public void execute(final SenderInfo sender, final String[] args) {
        if (!sender.hasMaintenancePermission("command")) {
            sender.send(settings.getMessage("noPermission"));
            return;
        }
        if (args.length == 0) {
            help.sendUsage(sender);
            return;
        }

        final CommandInfo command = commandExecutors.get(args[0].toLowerCase(Locale.ROOT));
        if (command == null) {
            help.sendUsage(sender);
            return;
        }
        if (!command.hasPermission(sender)) {
            sender.send(settings.getMessage("noPermission"));
            return;
        }

        command.execute(sender, args);
    }

    public List<String> getSuggestions(final SenderInfo sender, final String[] args) {
        if (!sender.hasMaintenancePermission("command") || args.length == 0) return Collections.emptyList();
        final String s = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1) {
            final List<String> list = new ArrayList<>();
            for (final Map.Entry<String, CommandInfo> entry : commandExecutors.entrySet()) {
                final String command = entry.getKey();
                final CommandInfo info = entry.getValue();
                if (info.isVisible() && command.startsWith(s) && info.hasPermission(sender)) {
                    list.add(command);
                }
            }
            return list;
        }

        final CommandInfo info = commandExecutors.get(args[0]);
        return info != null && info.hasPermission(sender) ? info.getTabCompletion(sender, args) : Collections.emptyList();
    }

    public @Nullable Duration parseDurationAndCheckTask(final SenderInfo sender, final String time, final boolean taskCheck) {
        final Duration duration;
        if (plugin.isNumeric(time)) {
            // Assume minutes by default as per old command behavior
            duration = Duration.ofMinutes(Integer.parseInt(time));
        } else {
            try {
                // Only accept hours, minutes, and seconds
                duration = Duration.parse("PT" + time.toUpperCase(Locale.ROOT));
            } catch (final DateTimeParseException e) {
                return null;
            }
        }

        if (taskCheck) {
            if (plugin.isTaskRunning()) {
                sender.send(settings.getMessage("timerAlreadyRunning"));
                return null;
            }
        }

        final long seconds = duration.getSeconds();
        if (seconds > MAX_TASK_DURATION_SECONDS) {
            sender.send(settings.getMessage("timerTooLong"));
            return null;
        }
        if (seconds < 1) {
            sender.sendRich("<i><dark_gray>[kennytv whispers to you] <gray>Think about running a timer for a negative amount of minutes. Doesn't work <b>that</b> <gray>well.");
            return null;
        }
        return duration;
    }

    public @Nullable Duration parseDurationAndCheckTask(final SenderInfo sender, final String time) {
        return parseDurationAndCheckTask(sender, time, true);
    }

    protected void addToggleAndTimerCommands() {
        add(new ToggleCommand(plugin), "on", "off");

        add(new StarttimerCommand(plugin), "starttimer", "start");
        add(new EndtimerCommand(plugin), "endtimer", "end");
        add(new ScheduleTimerCommand(plugin), "scheduletimer", "schedule");
        add(new AbortTimerCommand(plugin), "aborttimer", "abort");
    }

    public List<String> getServersCompletion(final String s) {
        return null;
    }

    public List<String> getMaintenanceServersCompletion(final String s) {
        return null;
    }

    public List<String> getPlayersCompletion() {
        return null;
    }

    public List<CommandInfo> getCommands() {
        return commands;
    }
}
