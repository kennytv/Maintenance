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

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MaintenanceCommand {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    private final List<CommandInfo> commandInfos = new ArrayList<>();
    private final Map<String, CommandInfo> tabCompleters = new LinkedHashMap<>();

    protected MaintenanceCommand(final MaintenanceModePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;

        // reload
        addCommandInfoWithTabCompleter("reload", "§6/maintenance reload §7(Reloads the config file, whitelist file and the server-icon)");

        // on, off, status
        final CommandInfo toggle = addCommandInfo("toggle", "§6/maintenance on §7(Enables maintenance mode)", "§6/maintenance off §7(Disables maintenance mode)");
        if (plugin.getServerType().isProxy()) {
            final CommandInfo toggleServer = new CommandInfo("toggleserver", args -> args.length == 2 ? getServersCompletion(args[1].toLowerCase()) : Collections.emptyList(),
                    "§6/maintenance on <server> §7(Enables maintenance mode on a specific proxied server)",
                    "§6/maintenance off <server> §7(Disables maintenance mode on a specific proxied server)");
            addCommandInfoWithTabCompleter("status", "§6/maintenance status §7(Lists all proxied servers, that are currently under maintenance)");
            commandInfos.add(toggleServer);
            tabCompleters.put("on", toggleServer);
            tabCompleters.put("off", new CommandInfo("toggleserver", args -> args.length == 2 ? getMaintenanceServersCompletion(args[1].toLowerCase()) : Collections.emptyList(), (String[]) null));
        } else {
            tabCompleters.put("on", toggle);
            tabCompleters.put("off", toggle);
        }

        // starttimer, endtimer, timer abort
        final CommandInfo timer = new CommandInfo("timer", args -> args[0].equalsIgnoreCase("timer") && args.length == 2 ? Collections.singletonList("abort") : Collections.emptyList(), "§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled)",
                "§6/maintenance endtimer <minutes> §7(After the given time in minutes, maintenance mode will be disabled)",
                "§6/maintenance timer abort §7(If running, the current timer will be aborted)");
        commandInfos.add(timer);
        if (plugin.getServerType().isProxy()) {
            final CommandInfo servertimer = new CommandInfo("servertimer", args -> args.length == 2 ? getServersCompletion(args[1]) : Collections.emptyList(),
                    "§6/maintenance starttimer <server> <minutes> §7(After the given time in minutes, maintenance mode will be enabled on the given server)",
                    "§6/maintenance endtimer <server> <minutes> §7(After the given time in minutes, maintenance mode will be disabled on the given server)",
                    "§6/maintenance timer abort <server> §7(If running, the timer running for that server will be aborted)");
            commandInfos.add(servertimer);
            tabCompleters.put("starttimer", servertimer);
            tabCompleters.put("endtimer", servertimer);
            tabCompleters.put("timer", servertimer);
        } else {
            tabCompleters.put("starttimer", timer);
            tabCompleters.put("endtimer", timer);
            tabCompleters.put("timer", timer);
        }

        // whitelist, add, remove
        tabCompleters.put("whitelist", addCommandInfo("whitelist.list", "§6/maintenance whitelist §7(Shows all whitelisted players for the maintenance mode)"));
        addCommandInfoWithTabCompleter("add", args -> args.length == 2 ? null : Collections.emptyList(), "whitelist.add", "§6/maintenance add <name/uuid> §7(Adds the player to the maintenance whitelist, so they can join the server even though maintenance is enabled)");
        addCommandInfoWithTabCompleter("remove", args -> args.length == 2 ?
                        settings.getWhitelistedPlayers().values().stream().filter(name -> name.toLowerCase().startsWith(args[1])).collect(Collectors.toList()) : Collections.emptyList(),
                "whitelist.remove", "§6/maintenance remove <name/uuid> §7(Removes the player from the maintenance whitelist)");

        // setmotd, motd
        addCommandInfoWithTabCompleter("setmotd", args -> {
            if (args.length == 3) return Arrays.asList("1", "2");
            if (args.length == 2) {
                final List<String> list = new ArrayList<>();
                for (int i = 1; i <= settings.getPingMessages().size() + 1; i++) {
                    list.add(String.valueOf(i));
                }
                return list;
            }
            return Collections.emptyList();
        }, "setmotd", "§6/maintenance setmotd <index> <1/2> <message> §7(Sets a motd for maintenance mode)", "§6/maintenance removemotd <index> §7(Removes a maintenance motd)");
        addCommandInfoWithTabCompleter("motd", "§6/maintenance motd §7(Lists the currently set maintenance motds)");

        // update, forceupdate
        final CommandInfo update = addCommandInfo("update", "§6/maintenance update §7(Remotely downloads the newest version of the plugin onto your server)");
        tabCompleters.put("update", update);
        tabCompleters.put("forceupdate", update);
    }

    // Adds the commandinfo without adding the tabcompleter.
    private CommandInfo addCommandInfo(final String permission, final String... messages) {
        final CommandInfo info = new CommandInfo(permission, messages);
        commandInfos.add(info);
        return info;
    }

    // Adds the commandinfo with an empty tabcompleter.
    private void addCommandInfoWithTabCompleter(final String command, final String... messages) {
        final CommandInfo info = new CommandInfo(command, messages);
        commandInfos.add(info);
        tabCompleters.put(command, info);
    }

    // Adds the commandinfo with a custom tabcompleter.
    private void addCommandInfoWithTabCompleter(final String command, final CommandInfo.TabCompleteCallback tabCompleteCallback, final String permission, final String... messages) {
        final CommandInfo info = new CommandInfo(permission, tabCompleteCallback, messages);
        commandInfos.add(info);
        tabCompleters.put(command, info);
    }

    public void execute(final SenderInfo sender, final String[] args) {
        if (checkPermission(sender, "command")) return;
        final String firstArg = args.length > 0 ? args[0] : null;
        if (args.length == 1) {
            if (firstArg.equals("on") || firstArg.equals("off")) {
                if (checkPermission(sender, "toggle")) return;

                final boolean maintenance = firstArg.equals("on");
                if (maintenance == plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
                    return;
                }

                plugin.setMaintenance(maintenance);
            } else if (firstArg.equals("reload")) {
                if (checkPermission(sender, "reload")) return;
                settings.reloadConfigs();
                sender.sendMessage(settings.getMessage("reload"));
            } else if (firstArg.equals("update")) {
                if (checkPermission(sender, "update")) return;
                checkForUpdate(sender);
            } else if (firstArg.equals("forceupdate")) {
                if (checkPermission(sender, "update")) return;
                sender.sendMessage(settings.getMessage("updateDownloading"));
                if (plugin.installUpdate())
                    sender.sendMessage(settings.getMessage("updateFinished"));
                else
                    sender.sendMessage(settings.getMessage("updateFailed"));
            } else if (firstArg.equals("whitelist")) {
                if (checkPermission(sender, "whitelist.list")) return;
                final Map<UUID, String> players = settings.getWhitelistedPlayers();
                if (players.isEmpty()) {
                    sender.sendMessage(settings.getMessage("whitelistEmtpy"));
                } else if (players.size() == 1 && players.containsKey(UUID.fromString("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"))) {
                    sender.sendMessage(settings.getMessage("whitelistEmptyDefault"));
                } else {
                    sender.sendMessage(settings.getMessage("whitelistedPlayers"));
                    final String format = settings.getMessage("whitelistedPlayersFormat");
                    players.forEach((key, value) -> sender.sendMessage(format.replace("%NAME%", value).replace("%UUID%", key.toString())));
                    sender.sendMessage("");
                }
            } else if (firstArg.equals("motd")) {
                if (checkPermission(sender, "motd")) return;
                sender.sendMessage(settings.getMessage("motdList"));
                for (int i = 0; i < settings.getPingMessages().size(); i++) {
                    sender.sendMessage("§b" + (i + 1) + "§8§m---------");
                    for (final String motd : settings.getColoredString(settings.getPingMessages().get(i)).split("%NEWLINE%")) {
                        sender.sendMessage(motd);
                    }
                }
                sender.sendMessage("§8§m----------");
            } else if (firstArg.equals("status") && plugin.getServerType().isProxy()) {
                if (checkPermission(sender, "status")) return;
                showMaintenanceStatus(sender);
            } else
                sendUsage(sender);
        } else if (args.length == 2) {
            if (firstArg.equals("help")) {
                if (!isNumeric(args[1])) {
                    sender.sendMessage(settings.getMessage("helpUsage"));
                    return;
                }
                sendUsage(sender, Integer.parseInt(args[1]));
            } else if ((firstArg.equals("on") || firstArg.equals("off")) && plugin.getServerType().isProxy()) {
                if (checkPermission(sender, "toggleserver")) return;
                handleToggleServerCommand(sender, args);
            } else if (firstArg.equals("endtimer")) {
                if (checkPermission(sender, "timer")) return;
                if (checkTimerArgs(sender, args[1], "endtimerUsage")) return;
                if (!plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage("alreadyDisabled"));
                    return;
                }
                plugin.startMaintenanceRunnable(Integer.parseInt(args[1]), false);
                sender.sendMessage(settings.getMessage("endtimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
            } else if (firstArg.equals("starttimer")) {
                if (checkPermission(sender, "timer")) return;
                if (checkTimerArgs(sender, args[1], "starttimerUsage")) return;
                if (plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage("alreadyEnabled"));
                    return;
                }

                plugin.startMaintenanceRunnable(Integer.parseInt(args[1]), true);
                sender.sendMessage(settings.getMessage("starttimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
            } else if (firstArg.equals("timer")) {
                if (args[1].equalsIgnoreCase("abort") || args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("cancel")) {
                    if (checkPermission(sender, "timer")) return;
                    if (!plugin.isTaskRunning()) {
                        sender.sendMessage(settings.getMessage("timerNotRunning"));
                        return;
                    }

                    plugin.cancelTask();
                    sender.sendMessage(settings.getMessage("timerCancelled"));
                } else
                    sendUsage(sender);
            } else if (firstArg.equals("add")) {
                if (checkPermission(sender, "whitelist.add")) return;
                if (args[1].length() == 36)
                    addPlayerToWhitelist(sender, UUID.fromString(args[1]));
                else
                    addPlayerToWhitelist(sender, args[1]);
            } else if (firstArg.equals("remove")) {
                if (checkPermission(sender, "whitelist.remove")) return;
                if (args[1].length() == 36)
                    removePlayerFromWhitelist(sender, UUID.fromString(args[1]));
                else
                    removePlayerFromWhitelist(sender, args[1]);
            } else if (firstArg.equals("removemotd")) {
                if (checkPermission(sender, "setmotd")) return;
                if (!isNumeric(args[1])) {
                    sender.sendMessage(settings.getMessage("removeMotdUsage"));
                    return;
                }
                if (settings.getPingMessages().size() < 2) {
                    sender.sendMessage(settings.getMessage("removeMotdError"));
                    return;
                }

                final int index = Integer.parseInt(args[1]);
                if (index > settings.getPingMessages().size()) {
                    sender.sendMessage(settings.getMessage("setMotdIndexError").replace("%MOTDS%", Integer.toString(settings.getPingMessages().size()))
                            .replace("%NEWAMOUNT%", Integer.toString(settings.getPingMessages().size())));
                    return;
                }

                settings.getPingMessages().remove(index - 1);
                settings.getConfig().set("pingmessages", settings.getPingMessages());
                settings.saveConfig();
                sender.sendMessage(settings.getMessage("removedMotd").replace("%INDEX%", args[1]));
            } else
                sendUsage(sender);
        } else if (args.length == 3 && plugin.getServerType().isProxy()) {
            handleTimerServerCommands(sender, args);
        } else if (args.length > 3 && firstArg.equals("setmotd")) {
            if (checkPermission(sender, "setmotd")) return;
            if (!isNumeric(args[1])) {
                sender.sendMessage(settings.getMessage("setMotdUsage"));
                return;
            }

            final int index = Integer.parseInt(args[1]);
            if (index < 1 || index > settings.getPingMessages().size() + 1) {
                sender.sendMessage(settings.getMessage("setMotdIndexError").replace("%MOTDS%", Integer.toString(settings.getPingMessages().size()))
                        .replace("%NEWAMOUNT%", Integer.toString(settings.getPingMessages().size() + 1)));
                return;
            }

            if (!isNumeric(args[2])) {
                sender.sendMessage(settings.getMessage("setMotdLineError"));
                return;
            }

            final int line = Integer.parseInt(args[2]);
            if (line != 1 && line != 2) {
                sender.sendMessage(settings.getMessage("setMotdLineError"));
                return;
            }

            final String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            final String oldMessage = index > settings.getPingMessages().size() ? "" : settings.getPingMessages().get(index - 1);
            final String newMessage;
            if (line == 1)
                newMessage = oldMessage.contains("%NEWLINE%") ?
                        message + "%NEWLINE%" + oldMessage.split("%NEWLINE%", 2)[1] : message;
            else
                newMessage = oldMessage.contains("%NEWLINE%") ?
                        oldMessage.split("%NEWLINE%", 2)[0] + "%NEWLINE%" + message : oldMessage + "%NEWLINE%" + message;

            if (index > settings.getPingMessages().size())
                settings.getPingMessages().add(newMessage);
            else
                settings.getPingMessages().set(index - 1, newMessage);
            settings.getConfig().set("pingmessages", settings.getPingMessages());
            settings.saveConfig();
            sender.sendMessage(settings.getMessage("setMotd").replace("%LINE%", args[2]).replace("%INDEX%", args[1])
                    .replace("%MOTD%", "§f" + settings.getColoredString(message)));
        } else
            sendUsage(sender);
    }

    public List<String> getSuggestions(final SenderInfo sender, final String[] args) {
        if (!sender.hasPermission("maintenance.command") || args.length == 0) return Collections.emptyList();
        final String s = args[0].toLowerCase();
        if (args.length == 1)
            return tabCompleters.entrySet().stream().filter(entry -> entry.getKey().startsWith(s) && entry.getValue().hasPermission(sender)).map(Map.Entry::getKey).collect(Collectors.toList());
        final CommandInfo info = tabCompleters.get(args[0]);
        return info == null ? Collections.emptyList() : info.getTabCompletion(args);
    }

    private static final int COMMANDS_PER_PAGE = 8;

    protected void sendUsage(final SenderInfo sender) {
        sendUsage(sender, 1);
    }

    protected void sendUsage(final SenderInfo sender, final int page) {
        final List<String> commands = new ArrayList<>();
        commandInfos.stream().filter(cmd -> cmd.hasPermission(sender)).forEach(cmd -> {
            for (final String message : cmd.getMessages()) {
                commands.add(message);
            }
        });
        if ((page - 1) * COMMANDS_PER_PAGE > commands.size()) {
            sender.sendMessage(settings.getMessage("helpPageNotFound"));
            return;
        }

        final List<String> filteredCommands;
        if (page * COMMANDS_PER_PAGE >= commands.size())
            filteredCommands = commands.subList((page - 1) * COMMANDS_PER_PAGE, commands.size());
        else
            filteredCommands = commands.subList((page - 1) * COMMANDS_PER_PAGE, page * COMMANDS_PER_PAGE);

        sender.sendMessage("");
        sender.sendMessage("§8========[ §eMaintenance" + plugin.getServerType() + " §8| §eVersion: §e" + plugin.getVersion() + " §8]========");
        filteredCommands.forEach(sender::sendMessage);
        if (page * 10 < commands.size())
            sender.sendMessage("§7Use §b/maintenance help " + (page + 1) + " §7to get to the next help window.");
        else
            sender.sendMessage("§8× §7Created by §bKennyTV");
        sender.sendMessage("§8========[ §eMaintenance" + plugin.getServerType() + " §8| §e" + page + "/" + ((commands.size() + getDivide(commands.size())) / COMMANDS_PER_PAGE) + " §8]========");
        sender.sendMessage("");
    }

    private int getDivide(final int size) {
        final int commandSize = size % COMMANDS_PER_PAGE;
        return commandSize > 0 ? COMMANDS_PER_PAGE - commandSize : 0;
    }

    protected boolean checkTimerArgs(final SenderInfo sender, final String time, final String usageKey) {
        if (!isNumeric(time)) {
            sender.sendMessage(settings.getMessage(usageKey));
            return true;
        }
        if (plugin.isTaskRunning()) {
            sender.sendMessage(settings.getMessage("timerAlreadyRunning"));
            return true;
        }

        final int minutes = Integer.parseInt(time);
        if (minutes > 40320) {
            sender.sendMessage(settings.getMessage("timerTooLong"));
            return true;
        }
        if (minutes < 1) {
            sender.sendMessage("§8§o[KennyTV whispers to you] §c§oThink about running a timer for a negative amount of minutes. Doesn't work §lthat §c§owell.");
            return true;
        }
        return false;
    }

    protected boolean checkPermission(final SenderInfo sender, final String permission) {
        if (!sender.hasPermission("maintenance." + permission)) {
            sender.sendMessage(settings.getMessage("noPermission"));
            return true;
        }
        return false;
    }

    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the server to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            sender.sendMessage(plugin.getPrefix() + "§eUse §c§l/maintenance forceupdate §eto update!");
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }

    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final SenderInfo selected = plugin.getPlayer(name);
        if (selected == null) {
            sender.sendMessage(settings.getMessage("playerNotOnline"));
            return;
        }

        whitelistAddMessage(sender, selected);
    }

    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final SenderInfo selected = plugin.getPlayer(name);
        if (selected == null) {
            if (settings.removeWhitelistedPlayer(name))
                sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", name));
            else
                sender.sendMessage(settings.getMessage("whitelistNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, selected);
    }

    protected void addPlayerToWhitelist(final SenderInfo sender, final UUID uuid) {
        final SenderInfo selected = plugin.getOfflinePlayer(uuid);
        if (selected == null) {
            sender.sendMessage(settings.getMessage("playerNotFoundUuid"));
            return;
        }

        whitelistAddMessage(sender, selected);
    }

    protected void removePlayerFromWhitelist(final SenderInfo sender, final UUID uuid) {
        final SenderInfo selected = plugin.getOfflinePlayer(uuid);
        if (selected == null) {
            if (settings.removeWhitelistedPlayer(uuid))
                sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", uuid.toString()));
            else
                sender.sendMessage(settings.getMessage("whitelistNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, selected);
    }

    protected void whitelistAddMessage(final SenderInfo sender, final SenderInfo selected) {
        if (settings.addWhitelistedPlayer(selected.getUuid(), selected.getName()))
            sender.sendMessage(settings.getMessage("whitelistAdded").replace("%PLAYER%", selected.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistAlreadyAdded").replace("%PLAYER%", selected.getName()));
    }

    protected void whitelistRemoveMessage(final SenderInfo sender, final SenderInfo selected) {
        if (settings.removeWhitelistedPlayer(selected.getUuid()))
            sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", selected.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistNotFound"));
    }

    private boolean isNumeric(final String string) {
        return string.matches("[0-9]+");
    }

    protected void showMaintenanceStatus(SenderInfo sender) {
    }

    protected void handleToggleServerCommand(SenderInfo sender, String[] args) {
    }

    protected void handleTimerServerCommands(SenderInfo sender, String[] args) {
    }

    protected List<String> getServersCompletion(String s) {
        return null;
    }

    protected List<String> getMaintenanceServersCompletion(String s) {
        return null;
    }
}
