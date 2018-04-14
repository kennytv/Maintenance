package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.MessageUtil;
import eu.kennytv.maintenance.core.util.SenderInfo;

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
        if (!sender.hasPermission("maintenance.admin")) {
            sender.sendMessage(settings.getNoPermMessage());
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                plugin.setMaintenance(true);
            } else if (args[0].equalsIgnoreCase("off")) {
                plugin.setMaintenance(false);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("maintenance.reload")) {
                    sender.sendMessage(settings.getNoPermMessage());
                    return;
                }

                settings.reloadConfigs();
                sender.sendMessage(plugin.getPrefix() + "§aReloaded config.yml");
            } else if (args[0].equalsIgnoreCase("update")) {
                if (!sender.hasPermission("maintenance.admin")) {
                    sender.sendMessage(settings.getNoPermMessage());
                    return;
                }

                checkForUpdate(sender);
            } else if (args[0].equals("forceupdate")) {
                if (!sender.hasPermission("maintenance.admin")) {
                    sender.sendMessage(settings.getNoPermMessage());
                    return;
                }

                sender.sendMessage(plugin.getPrefix() + "§c§lDownloading update...");

                if (plugin.installUpdate())
                    sender.sendMessage(plugin.getPrefix() + "§a§lThe update was successful! To prevent issues with tasks and to complete the update, you have to restart the proxy!");
                else
                    sender.sendMessage(plugin.getPrefix() + "§4Failed!");
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                final Map<UUID, String> players = settings.getWhitelistedPlayers();
                if (players.isEmpty()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe maintenance whitelist is empty! Use \"/maintenance add <player>\" to add someone!");
                } else if (players.size() == 1 && players.containsKey(UUID.fromString("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"))) {
                    sender.sendMessage(plugin.getPrefix() + "§cUse \"/maintenance add <player>\" to add someone. Alternatively, you can add the uuid of a player to the WhitelistedPlayers.yml as seen in the example in the file!");
                } else {
                    sender.sendMessage("§6Whitelisted players for maintenance:");
                    players.forEach((key, value) -> sender.sendMessage("§8- §e" + value));
                    sender.sendMessage("");
                }
            } else
                sendUsage(sender);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("endtimer")) {
                if (!MessageUtil.isNumeric(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + "§6/maintenance timer endtimer <minutes>");
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThere's already a starttimer scheduled!");
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe number has to be less than 40320 (28 days)!");
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage(plugin.getPrefix() + "§cThink about running a timer for a negative amount of minutes. Doesn't work §othat §r§cwell.");
                    return;
                }

                plugin.setMaintenance(true);
                sender.sendMessage(plugin.getPrefix() + "§aStarted timer: §7Maintenance mode will be deactivated in §6" + minutes + " minutes§7.");
                plugin.setTaskId(plugin.schedule(new MaintenanceRunnable(plugin, settings, minutes, false)));
            } else if (args[0].equalsIgnoreCase("starttimer")) {
                if (!MessageUtil.isNumeric(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + "§6/maintenance timer starttimer <minutes>");
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThere's already running a timer!");
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe number has to be less than 40320 (28 days)!");
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage(plugin.getPrefix() + "§cThink about running a timer for a negative amount of minutes. Doesn't work §othat §r§cwell.");
                    return;
                }

                sender.sendMessage(plugin.getPrefix() + "§aStarted timer: §7Maintenance mode will be activated in §6" + minutes + " minutes§7.");
                plugin.setTaskId(plugin.schedule(new MaintenanceRunnable(plugin, settings, minutes, true)));
            } else if (args[0].equalsIgnoreCase("timer")) {
                if (args[1].equalsIgnoreCase("abort") || args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("cancel")) {
                    if (!plugin.isTaskRunning()) {
                        sender.sendMessage(plugin.getPrefix() + "§cThere's currently no running timer.");
                        return;
                    }

                    plugin.cancelTask();
                    sender.sendMessage(plugin.getPrefix() + "§cThe timer has been disabled.");
                } else
                    sendUsage(sender);
            } else if (args[0].equalsIgnoreCase("add")) {
                addPlayerToWhitelist(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("remove")) {
                removePlayerFromWhitelist(sender, args[1]);
            } else
                sendUsage(sender);
        } else
            sendUsage(sender);
    }

    private void sendUsage(final SenderInfo sender) {
        sender.sendMessage("");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        sender.sendMessage("§6/maintenance reload §7(Reloads the config file)");
        sender.sendMessage("§6/maintenance on §7(Enables maintenance mode");
        sender.sendMessage("§6/maintenance off §7(Disables maintenance mode)");
        sender.sendMessage("§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled. Broadcast settings for the timer can be found in the config)");
        sender.sendMessage("§6/maintenance endtimer <minutes> §7(Enables maintenance mode. After the given time in minutes, maintenance mode will be disabled)");
        sender.sendMessage("§6/maintenance timer abort §7(If running, the current timer will be aborted)");
        sender.sendMessage("§6/maintenance add <player> §7(Adds the player to the maintenance whitelist, so they can join the server even though maintenance is enabled)");
        sender.sendMessage("§6/maintenance remove <player> §7(Removes the player from the maintenance whitelist)");
        sender.sendMessage("§6/maintenance whitelist §7(Shows all whitelisted players for the maintenance mode)");
        sender.sendMessage("§6/maintenance update §7(Remotely downloads the newest version of the plugin onto your server)");
        sender.sendMessage("§9Created by: KennyTV");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        sender.sendMessage("");
    }

    protected abstract void addPlayerToWhitelist(SenderInfo sender, String name);

    protected abstract void removePlayerFromWhitelist(SenderInfo sender, String name);

    protected abstract void checkForUpdate(SenderInfo sender);
}
