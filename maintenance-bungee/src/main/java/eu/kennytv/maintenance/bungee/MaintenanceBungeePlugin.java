package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.bungee.IMaintenanceBungee;
import eu.kennytv.maintenance.api.bungee.MaintenanceBungeeAPI;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommand;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommandBase;
import eu.kennytv.maintenance.bungee.listener.PostLoginListener;
import eu.kennytv.maintenance.bungee.listener.ServerConnectListener;
import eu.kennytv.maintenance.bungee.metrics.MetricsLite;
import eu.kennytv.maintenance.bungee.runnable.SingleMaintenanceRunnable;
import eu.kennytv.maintenance.bungee.util.ProxiedSenderInfo;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeePlugin extends MaintenanceModePlugin implements IMaintenanceBungee {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;
    private final Map<String, Integer> serverTaskIds = new HashMap<>();

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.BUNGEE);

        this.plugin = plugin;
        plugin.getLogger().info("Plugin by KennyTV");
        plugin.getLogger().info(getUpdateMessage());

        settings = new SettingsBungee(this, plugin);

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new PostLoginListener(this, settings));
        pm.registerListener(plugin, new ServerConnectListener(this, settings));
        final MaintenanceBungeeCommand maintenanceCommand = new MaintenanceBungeeCommand(this, settings);
        pm.registerCommand(plugin, new MaintenanceBungeeCommandBase(maintenanceCommand));

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (serverListPlus != null) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceBungeeAPI.getAPI();
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        if (settings.getMySQL() != null) {
            settings.setMaintenanceToSQL(maintenance);
        } else {
            settings.setMaintenance(maintenance);
            settings.setToConfig("enable-maintenance-mode", maintenance);
            settings.saveConfig();
        }

        serverActions(maintenance);
        if (isTaskRunning())
            cancelTask();
    }

    void serverActions(final boolean maintenance) {
        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            getProxy().getPlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n")));
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public boolean isMaintenance(final ServerInfo server) {
        return settings.isMaintenance(server);
    }

    @Override
    public boolean setMaintenanceToServer(final ServerInfo server, final boolean maintenance) {
        if (maintenance) {
            if (!settings.addMaintenanceServer(server.getName())) return false;
        } else {
            if (!settings.removeMaintenanceServer(server.getName())) return false;
        }
        serverActions(server, maintenance);
        cancelSingleTask(server);
        return true;
    }

    void serverActions(final ServerInfo server, final boolean maintenance) {
        if (maintenance) {
            final ServerInfo fallback = getProxy().getServerInfo(settings.getFallbackServer());
            if (fallback == null && !server.getPlayers().isEmpty())
                plugin.getLogger().warning("The fallback server set in the SpigotServers.yml could not be found! Instead kicking players from that server off the network!");
            else if (fallback.equals(server))
                plugin.getLogger().warning("Maintenance has been enabled on the fallback server! If a player joins on a proxied server, they will be kicked completely instead of being sent to the fallback server!");
            server.getPlayers().forEach(p -> {
                if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                    if (fallback != null && fallback.canAccess(p)) {
                        p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
                        p.connect(fallback);
                    } else
                        p.disconnect(settings.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", server.getName()));
                } else {
                    p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
                }
            });
        } else
            server.getPlayers().forEach(p -> p.sendMessage(settings.getMessage("singleMaintenanceDeactivated").replace("%SERVER%", server.getName())));
    }

    @Override
    public boolean isServerTaskRunning(final ServerInfo server) {
        return serverTaskIds.containsKey(server.getName());
    }

    @Override
    protected int startMaintenanceRunnable(final Runnable runnable) {
        return getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId();
    }

    public MaintenanceRunnableBase startSingleMaintenanceRunnable(final ServerInfo server, final int minutes, final boolean enable) {
        final MaintenanceRunnableBase runnable = new SingleMaintenanceRunnable(this, (Settings) getSettings(), minutes, enable, server);
        serverTaskIds.put(server.getName(), getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId());
        return runnable;
    }

    @Override
    public void async(final Runnable runnable) {
        getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void cancelTask() {
        getProxy().getScheduler().cancel(taskId);
        runnable = null;
        taskId = 0;
    }

    public void cancelSingleTask(final ServerInfo server) {
        final Integer task = serverTaskIds.remove(server.getName());
        if (task != null)
            getProxy().getScheduler().cancel(task);
    }

    @Override
    public void broadcast(final String message) {
        getProxy().broadcast(message);
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        sender.sendMessage(getPrefix() + "§cThere is a newer version available: §aVersion " + getNewestVersion() + "§c, you're on §a" + getVersion());
        final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(getPrefix()));
        final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
        final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenancemode.40699/"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
        tc1.addExtra(tc2);
        tc1.addExtra(click);
        ((ProxiedSenderInfo) sender).sendMessage(tc1);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public File getPluginFile() {
        return plugin.getPluginFile();
    }

    @Override
    public InputStream getResource(final String name) {
        return plugin.getResourceAsStream(name);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }
}