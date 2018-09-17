package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public abstract class MaintenanceCommand {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    private final String name;

    protected MaintenanceCommand(final MaintenanceModePlugin plugin, final Settings settings, final String name) {
        this.plugin = plugin;
        this.settings = settings;
        this.name = name;
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
            } else
                sendUsage(sender);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("endtimer")) {
                if (checkPermission(sender, "timer")) return;
                if (!isNumeric(args[1])) {
                    sender.sendMessage(settings.getMessage("endtimerUsage"));
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(settings.getMessage("timerAlreadyRunning"));
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(settings.getMessage("timerTooLong"));
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage("§8§o[KennyTV whispers to you] §c§oThink about running a timer for a negative amount of minutes. Doesn't work §lthat §c§owell.");
                    return;
                }

                if (!plugin.isMaintenance())
                    plugin.setMaintenance(true);
                plugin.startMaintenanceRunnable(minutes, false);
                sender.sendMessage(settings.getMessage("endtimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
            } else if (args[0].equalsIgnoreCase("starttimer")) {
                if (checkPermission(sender, "timer")) return;
                if (!isNumeric(args[1])) {
                    sender.sendMessage(settings.getMessage("starttimerUsage"));
                    return;
                }
                if (plugin.isMaintenance()) {
                    sender.sendMessage(settings.getMessage("alreadyEnabled"));
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(settings.getMessage("timerAlreadyRunning"));
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(settings.getMessage("timerTooLong"));
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage("§8§o[KennyTV whispers to you] §c§oThink about running a timer for a negative amount of minutes. Doesn't work §lthat §c§owell.");
                    return;
                }

                sender.sendMessage(settings.getMessage("starttimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
                plugin.startMaintenanceRunnable(minutes, true);
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
                settings.reloadConfigs();
                sender.sendMessage(settings.getMessage("removedMotd").replace("%INDEX%", args[1]));
            } else
                sendUsage(sender);
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
            settings.reloadConfigs();
            sender.sendMessage(settings.getMessage("setMotd").replace("%LINE%", args[2]).replace("%INDEX%", args[1])
                    .replace("%MOTD%", "§f" + settings.getColoredString(message)));
        } else
            sendUsage(sender);
    }

    private void sendUsage(final SenderInfo sender) {
        sender.sendMessage("");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        if (sender.hasPermission("maintenance.reload"))
            sender.sendMessage("§6/maintenance reload §7(Reloads the config file, whitelist file and the server-icon)");
        if (sender.hasPermission("maintenance.toggle")) {
            sender.sendMessage("§6/maintenance on §7(Enables maintenance mode");
            sender.sendMessage("§6/maintenance off §7(Disables maintenance mode)");
        }
        if (sender.hasPermission("maintenance.timer")) {
            sender.sendMessage("§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled)");
            sender.sendMessage("§6/maintenance endtimer <minutes> §7(Enables maintenance mode. After the given time in minutes, maintenance mode will be disabled)");
            sender.sendMessage("§6/maintenance timer abort §7(If running, the current timer will be aborted)");
        }
        if (sender.hasPermission("maintenance.whitelist.list"))
            sender.sendMessage("§6/maintenance whitelist §7(Shows all whitelisted players for the maintenance mode)");
        if (sender.hasPermission("maintenance.whitelist.add"))
            sender.sendMessage("§6/maintenance add <player> §7(Adds the player to the maintenance whitelist, so they can join the server even though maintenance is enabled)");
        if (sender.hasPermission("maintenance.whitelist.remove"))
            sender.sendMessage("§6/maintenance remove <player> §7(Removes the player from the maintenance whitelist)");
        if (sender.hasPermission("maintenance.setmotd")) {
            sender.sendMessage("§6/maintenance setmotd <index> <1/2> <message> §7(Sets a motd for maintenance mode)");
            sender.sendMessage("§6/maintenance removemotd <index> §7(Removes a maintenance motd)");
        }
        if (sender.hasPermission("maintenance.motd"))
            sender.sendMessage("§6/maintenance motd §7(Lists the currently set maintenance motds)");
        if (sender.hasPermission("maintenance.update"))
            sender.sendMessage("§6/maintenance update §7(Remotely downloads the newest version of the plugin onto your server)");
        sender.sendMessage("§8× §7Created by §bKennyTV");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        sender.sendMessage("");
    }

    private boolean checkPermission(final SenderInfo sender, final String permission) {
        if (!sender.hasPermission("maintenance." + permission)) {
            sender.sendMessage(settings.getMessage("noPermission"));
            return true;
        }
        return false;
    }

    private boolean isNumeric(final String string) {
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
}
