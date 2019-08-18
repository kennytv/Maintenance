/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
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
import eu.kennytv.maintenance.core.command.subcommand.*;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;

import java.util.*;

public abstract class MaintenanceCommand {
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
    }

    protected void add(final CommandInfo command, final String... aliases) {
        commands.add(command);
        for (final String alias : aliases) {
            commandExecutors.put(alias, command);
        }
    }

    public void execute(final SenderInfo sender, final String[] args) {
        if (!sender.hasMaintenancePermission("command")) {
            sender.sendMessage(settings.getMessage("noPermission"));
            return;
        }
        if (args.length == 0) {
            help.sendUsage(sender);
            return;
        }

        final CommandInfo command = commandExecutors.get(args[0].toLowerCase());
        if (command == null) {
            help.sendUsage(sender);
            return;
        }
        if (!command.hasPermission(sender)) {
            sender.sendMessage(settings.getMessage("noPermission"));
            return;
        }

        command.execute(sender, args);
    }

    public List<String> getSuggestions(final SenderInfo sender, final String[] args) {
        if (!sender.hasMaintenancePermission("command") || args.length == 0) return Collections.emptyList();
        final String s = args[0].toLowerCase();
        if (args.length == 1) {
            final List<String> list = new ArrayList<>();
            for (final Map.Entry<String, CommandInfo> entry : commandExecutors.entrySet()) {
                final String command = entry.getKey();
                if (command.startsWith(s) && entry.getValue().hasPermission(sender)) {
                    list.add(command);
                }
            }
            return list;
        }

        final CommandInfo info = commandExecutors.get(args[0]);
        return info != null && info.hasPermission(sender) ? info.getTabCompletion(sender, args) : Collections.emptyList();
    }

    public boolean checkTimerArgs(final SenderInfo sender, final String time, final boolean taskCheck) {
        if (!plugin.isNumeric(time)) return true;
        if (taskCheck) {
            if (plugin.isTaskRunning()) {
                sender.sendMessage(settings.getMessage("timerAlreadyRunning"));
                return true;
            }
        }

        final int minutes = Integer.parseInt(time);
        if (minutes > 40320) {
            sender.sendMessage(settings.getMessage("timerTooLong"));
            return true;
        }
        if (minutes < 1) {
            sender.sendMessage("§8§o[KennyTV whispers to you] §7§oThink about running a timer for a negative amount of minutes. Doesn't work §lthat §7§owell.");
            return true;
        }
        return false;
    }

    public boolean checkTimerArgs(final SenderInfo sender, final String time) {
        return checkTimerArgs(sender, time, true);
    }

    protected void addToggleAndTimerCommands() {
        add(new ToggleCommand(plugin), "on", "off");

        add(new StarttimerCommand(plugin), "starttimer", "start");
        add(new EndtimerCommand(plugin), "endtimer", "end");
        add(new AbortTimerCommand(plugin), "aborttimer", "abort");
    }

    public void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            // Ore sad :(
            if (plugin.getServerType() != ServerType.SPONGE) {
                sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the server to prevent further issues and to complete the update! If you can't do that, don't update!");
                sendUpdateMessage(sender);
            } else {
                sender.sendMessage(plugin.getPrefix() + "§aDownload the latest version on the Ore plugin page: §bhttps://ore.spongepowered.org/KennyTV/Maintenance");
            }
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }

    protected void sendUpdateMessage(final SenderInfo sender) {
        sender.sendMessage(plugin.getPrefix() + "§eUse §c§l/maintenance forceupdate §eto update!");
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

    public abstract void sendDumpMessage(final SenderInfo sender, final String url);

    public List<CommandInfo> getCommands() {
        return commands;
    }
}
