package eu.kennytv.maintenance.bungee.command;

import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import eu.kennytv.maintenance.bungee.util.ProxiedSenderInfo;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class MaintenanceBungeeCommand extends MaintenanceCommand {
    private final MaintenanceBungeePlugin plugin;
    private final SettingsBungee settingsBungee;

    public MaintenanceBungeeCommand(final MaintenanceBungeePlugin plugin, final SettingsBungee settings) {
        super(plugin, settings, "MaintenanceBungee");
        this.plugin = plugin;
        settingsBungee = settings;
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final ProxiedPlayer selected = plugin.getProxy().getPlayer(name);
        if (selected == null) {
            sender.sendMessage(settings.getMessage("playerNotOnline"));
            return;
        }

        if (settings.addWhitelistedPlayer(selected.getUniqueId(), selected.getName()))
            sender.sendMessage(settings.getMessage("whitelistAdded").replace("%PLAYER%", selected.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistAlreadyAdded").replace("%PLAYER%", selected.getName()));
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final ProxiedPlayer selected = plugin.getProxy().getPlayer(name);
        if (selected == null) {
            if (settings.removeWhitelistedPlayer(name))
                sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", name));
            else
                sender.sendMessage(settings.getMessage("whitelistNotFound"));
            return;
        }

        if (settings.removeWhitelistedPlayer(selected.getUniqueId()))
            sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", selected.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistNotFound"));
    }

    @Override
    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the proxy to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            final TextComponent tc = new TextComponent("§6× §8[§aUpdate§8]");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maintenance forceupdate"));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§aClick here to update the plugin")));
            tc.addExtra(" §8< §7Or use the command §c/maintenance forceupdate");

            ((ProxiedSenderInfo) sender).sendMessage(tc);
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }

    @Override
    protected void handleToggleServerCommand(final SenderInfo sender, final String args[]) {
        final ServerInfo server = plugin.getProxy().getServerInfo(args[1]);
        if (server == null) {
            sender.sendMessage(settings.getMessage("serverNotFound"));
            return;
        }

        final boolean maintenance = args[0].equalsIgnoreCase("on");
        if (maintenance == settingsBungee.getMaintenanceServers().contains(server.getName())) {
            sender.sendMessage(settings.getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
            return;
        }

        if (!plugin.setMaintenanceToServer(server, maintenance))
            sender.sendMessage(settings.getMessage(maintenance ? "singleServerAlreadyEnabled" : "singleServerAlreadyDisabled"));
        else if (!plugin.getProxy().getPlayer(sender.getUuid()).getServer().getInfo().equals(server))
            sender.sendMessage(settings.getMessage(maintenance ? "singleMaintenanceActivated" : "singleMaintenanceDectivated"));
    }

    @Override
    protected void handleTimerServerCommands(final SenderInfo sender, final String args[]) {
        if (args[0].equalsIgnoreCase("endtimer")) {
            if (checkPermission(sender, "servertimer")) return;
            if (checkTimerArgs(sender, args[2], "singleEndtimerUsage")) return;

            final ServerInfo server = checkSingleTimerArgs(sender, args);
            if (server == null) return;
            if (!plugin.isMaintenance(server))
                plugin.setMaintenanceToServer(server, true);
            final MaintenanceRunnableBase runnable = plugin.startSingleMaintenanceRunnable(server, Integer.parseInt(args[2]), false);
            sender.sendMessage(settings.getMessage("endtimerStarted").replace("%TIME%", runnable.getTime()));
        } else if (args[0].equalsIgnoreCase("starttimer")) {
            if (checkPermission(sender, "servertimer")) return;
            if (checkTimerArgs(sender, args[2], "singleStarttimerUsage")) return;

            final ServerInfo server = checkSingleTimerArgs(sender, args);
            if (server == null) return;
            if (plugin.isMaintenance(server)) {
                sender.sendMessage(settings.getMessage("alreadyEnabled"));
                return;
            }

            final MaintenanceRunnableBase runnable = plugin.startSingleMaintenanceRunnable(server, Integer.parseInt(args[2]), true);
            sender.sendMessage(settings.getMessage("starttimerStarted").replace("%TIME%", runnable.getTime()));
        } else if (args[0].equalsIgnoreCase("timer")) {
            if (args[1].equalsIgnoreCase("abort") || args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("cancel")) {
                if (checkPermission(sender, "servertimer")) return;
                final ServerInfo server = plugin.getProxy().getServerInfo(args[2]);
                if (server == null) {
                    sender.sendMessage(settings.getMessage("serverNotFound"));
                    return;
                }
                if (!plugin.isServerTaskRunning(server)) {
                    sender.sendMessage(settings.getMessage("timerNotRunning"));
                    return;
                }

                plugin.cancelSingleTask(server);
                sender.sendMessage(settings.getMessage("timerCancelled"));
            } else
                sendUsage(sender);
        }
    }

    @Override
    protected void showMaintenanceStatus(final SenderInfo sender) {
        if (settingsBungee.getMaintenanceServers().isEmpty()) {
            sender.sendMessage(settings.getMessage("singleServerMaintenanceListEmpty"));
        } else {
            sender.sendMessage(settings.getMessage("singleServerMaintenanceList"));
            settingsBungee.getMaintenanceServers().forEach(server -> sender.sendMessage("§8- §b" + server));
        }
    }

    private ServerInfo checkSingleTimerArgs(final SenderInfo sender, final String[] args) {
        final ServerInfo server = plugin.getProxy().getServerInfo(args[1]);
        if (server == null) {
            sender.sendMessage(settings.getMessage("serverNotFound"));
            return null;
        }
        if (plugin.isServerTaskRunning(server)) {
            sender.sendMessage(settings.getMessage("timerAlreadyRunning"));
            return null;
        }
        return server;
    }
}
