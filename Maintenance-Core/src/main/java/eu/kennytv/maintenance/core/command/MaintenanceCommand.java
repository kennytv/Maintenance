package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;

import java.util.*;

public abstract class MaintenanceCommand {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    private final List<CommandInfo> commandInfos;
    private final String name;

    protected MaintenanceCommand(final MaintenanceModePlugin plugin, final Settings settings, final String name) {
        this.plugin = plugin;
        this.settings = settings;
        this.name = name;
        this.commandInfos = new ArrayList<>();
        addCommandInfo("reload", "§6/maintenance reload §7(Reloads the config file, whitelist file and the server-icon)");
        addCommandInfo("toggle", "§6/maintenance on §7(Enables maintenance mode)", "§6/maintenance off §7(Disables maintenance mode)");
        if (plugin.getServerType() == ServerType.BUNGEE) {
            addCommandInfo("toggleserver", "§6/maintenance on <server> §7(Enables maintenance mode on a specific proxied server)",
                    "§6/maintenance off <server> §7(Disables maintenance mode on a specific proxied server)");
        }
        addCommandInfo("timer", "§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled)",
                "§6/maintenance endtimer <minutes> §7(After the given time in minutes, maintenance mode will be disabled)",
                "§6/maintenance timer abort §7(If running, the current timer will be aborted)");
        if (plugin.getServerType() == ServerType.BUNGEE) {
            addCommandInfo("servertimer", "§6/maintenance starttimer <server> <minutes> §7(After the given time in minutes, maintenance mode will be enabled on the given server)",
                    "§6/maintenance endtimer <server> <minutes> §7(After the given time in minutes, maintenance mode will be disabled on the given server)",
                    "§6/maintenance timer abort <server> §7(If running, the timer running for that server will be aborted)");
        }
        addCommandInfo("whitelist.list", "§6/maintenance whitelist §7(Shows all whitelisted players for the maintenance mode)");
        addCommandInfo("whitelist.add", "§6/maintenance add <player> §7(Adds the player to the maintenance whitelist, so they can join the server even though maintenance is enabled)");
        addCommandInfo("whitelist.remove", "§6/maintenance remove <player> §7(Removes the player from the maintenance whitelist)");
        addCommandInfo("setmotd", "§6/maintenance setmotd <index> <1/2> <message> §7(Sets a motd for maintenance mode)", "§6/maintenance removemotd <index> §7(Removes a maintenance motd)");
        addCommandInfo("motd", "§6/maintenance motd §7(Lists the currently set maintenance motds)");
        addCommandInfo("update", "§6/maintenance update §7(Remotely downloads the newest version of the plugin onto your server)");
    }

    private void addCommandInfo(final String permission, final String... messages) {
        commandInfos.add(new CommandInfo(permission, messages));
    }

    public void execute(final SenderInfo sender, final String[] args) {
        if (checkPermission(sender, "command")) return;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                if (checkPermission(sender, "toggle")) return;

                final boolean maintenance = args[0].equalsIgnoreCase("on");
                if (maintenance == plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
                    return;
                }

                plugin.setMaintenance(maintenance);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (checkPermission(sender, "reload")) return;
                settings.reloadConfigs();
                sender.sendMessage(settings.getMessage("reload"));
            } else if (args[0].equalsIgnoreCase("update")) {
                if (checkPermission(sender, "update")) return;
                checkForUpdate(sender);
            } else if (args[0].equals("forceupdate")) {
                if (checkPermission(sender, "update")) return;
                sender.sendMessage(settings.getMessage("updateDownloading"));
                if (plugin.installUpdate())
                    sender.sendMessage(settings.getMessage("updateFinished"));
                else
                    sender.sendMessage(settings.getMessage("updateFailed"));
            } else if (args[0].equalsIgnoreCase("whitelist")) {
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
            } else if (args[0].equalsIgnoreCase("motd")) {
                if (checkPermission(sender, "motd")) return;
                sender.sendMessage(settings.getMessage("motdList"));
                for (int i = 0; i < settings.getPingMessages().size(); i++) {
                    sender.sendMessage("§b" + (i + 1) + "§8§m---------");
                    for (final String motd : settings.getColoredString(settings.getPingMessages().get(i)).split("%NEWLINE%")) {
                        sender.sendMessage(motd);
                    }
                }
                sender.sendMessage("§8§m----------");
            } else if (args[0].equalsIgnoreCase("status") && plugin.getServerType() == ServerType.BUNGEE) {
                if (checkPermission(sender, "status")) return;
                showMaintenanceStatus(sender);
            } else
                sendUsage(sender);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                if (!isNumeric(args[1])) {
                    sender.sendMessage(settings.getMessage("helpUsage"));
                    return;
                }
                sendUsage(sender, Integer.parseInt(args[1]));
            } else if ((args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) && plugin.getServerType() == ServerType.BUNGEE) {
                if (checkPermission(sender, "toggleserver")) return;
                handleToggleServerCommand(sender, args);
            } else if (args[0].equalsIgnoreCase("endtimer")) {
                if (checkPermission(sender, "timer")) return;
                if (checkTimerArgs(sender, args[1], "endtimerUsage")) return;
                if (!plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage("alreadyDisabled"));
                    return;
                }
                plugin.startMaintenanceRunnable(Integer.parseInt(args[1]), false);
                sender.sendMessage(settings.getMessage("endtimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
            } else if (args[0].equalsIgnoreCase("starttimer")) {
                if (checkPermission(sender, "timer")) return;
                if (checkTimerArgs(sender, args[1], "starttimerUsage")) return;
                if (plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage("alreadyEnabled"));
                    return;
                }

                plugin.startMaintenanceRunnable(Integer.parseInt(args[1]), true);
                sender.sendMessage(settings.getMessage("starttimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
            } else if (args[0].equalsIgnoreCase("timer")) {
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
            } else if (args[0].equalsIgnoreCase("add")) {
                if (checkPermission(sender, "whitelist.add")) return;
                addPlayerToWhitelist(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (checkPermission(sender, "whitelist.remove")) return;
                removePlayerFromWhitelist(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("removemotd")) {
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
                settings.setToConfig("pingmessages", settings.getPingMessages());
                settings.saveConfig();
                sender.sendMessage(settings.getMessage("removedMotd").replace("%INDEX%", args[1]));
            } else
                sendUsage(sender);
        } else if (args.length == 3 && plugin.getServerType() == ServerType.BUNGEE) {
            handleTimerServerCommands(sender, args);
        } else if (args.length > 3 && args[0].equalsIgnoreCase("setmotd")) {
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
            settings.setToConfig("pingmessages", settings.getPingMessages());
            settings.saveConfig();
            sender.sendMessage(settings.getMessage("setMotd").replace("%LINE%", args[2]).replace("%INDEX%", args[1])
                    .replace("%MOTD%", "§f" + settings.getColoredString(message)));
        } else
            sendUsage(sender);
    }

    protected void sendUsage(final SenderInfo sender) {
        sendUsage(sender, 1);
    }

    private static final int COMMANDS_PER_PAGE = 8;

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
        sender.sendMessage("§8========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]========");
        filteredCommands.forEach(sender::sendMessage);
        if (page * 10 < commands.size())
            sender.sendMessage("§7Use §b/maintenance help " + (page + 1) + " §7to get to the next help window.");
        else
            sender.sendMessage("§8× §7Created by §bKennyTV");
        sender.sendMessage("§8========[ §e" + name + " §8| §e" + page + "/" + ((commands.size() + getDivide(commands.size())) / COMMANDS_PER_PAGE) + " §8]========");
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

    protected boolean isNumeric(final String string) {
        try {
            Integer.parseInt(string);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    protected abstract void addPlayerToWhitelist(SenderInfo sender, String name);

    protected abstract void removePlayerFromWhitelist(SenderInfo sender, String name);

    protected abstract void checkForUpdate(SenderInfo sender);

    protected void showMaintenanceStatus(SenderInfo sender) {
    }

    protected void handleToggleServerCommand(SenderInfo sender, String[] args) {
    }

    protected void handleTimerServerCommands(SenderInfo sender, String[] args) {
    }
}
