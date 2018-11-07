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
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeePlugin extends MaintenanceModePlugin implements IMaintenanceBungee {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;
    private final Map<String, Integer> serverTaskIds = new HashMap<>();

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super("§8[§eMaintenanceBungee§8] ", plugin.getDescription().getVersion(), ServerType.BUNGEE);

        this.plugin = plugin;
        plugin.getLogger().info("Plugin by KennyTV");
        plugin.getLogger().info(getUpdateMessage());

        settings = new SettingsBungee(this, plugin);

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new PostLoginListener(this, settings));
        pm.registerListener(plugin, new ServerConnectListener(settings));
        final MaintenanceBungeeCommand maintenanceCommand = new MaintenanceBungeeCommand(this, settings);
        pm.registerCommand(plugin, new MaintenanceBungeeCommandBase(maintenanceCommand));

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (serverListPlus != null) {
            serverListPlusHook = new ServerListPlusHook(((ServerListPlusPlugin) serverListPlus).getCore());
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        if (settings.getMySQL() != null) {
            settings.setMaintenanceToSQL(maintenance);
        } else {
            settings.setMaintenance(maintenance);
            settings.setToConfig("enable-maintenance-mode", maintenance);
            settings.saveConfig();
            settings.reloadConfigs();
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
            getProxy().broadcast(settings.getMessage("maintenanceActivated"));
        } else
            getProxy().broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public boolean isMaintenance(final ServerInfo server) {
        return settings.getMaintenanceServers().contains(server.getName());
    }

    @Override
    public boolean setMaintenanceToServer(final ServerInfo server, final boolean maintenance) {
        if (maintenance) {
            if (!settings.getMaintenanceServers().add(server.getName())) return false;

            final ServerInfo fallback = ProxyServer.getInstance().getServerInfo(settings.getFallbackServer());
            if (fallback == null)
                plugin.getLogger().warning("The fallback server set in the SpigotServers.yml could not be found! Instead kicking players from the network!");
            server.getPlayers().forEach(p -> {
                if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                    //TODO messages, yikes
                    if (fallback != null && fallback.canAccess(p)) {
                        p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
                        p.connect(fallback);
                    } else
                        p.disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n"));
                } else {
                    p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
                }
            });
        } else {
            if (!settings.getMaintenanceServers().remove(server.getName())) return false;
            server.getPlayers().forEach(p -> p.sendMessage(settings.getMessage("singleMaintenanceDeactivated").replace("%SERVER%", server.getName())));
        }

        /*if (mySQL != null) {
            mySQL.executeUpdate(serversQuery, "spigot-servers-with-maintenance", maintenanceServers, maintenanceServers);
        } else {
            spigotServers.set("maintenance-on", maintenanceServers);
            saveSpigotServers();
        }*/
        cancelSingleTask(server);
        settings.saveServersToConfig();
        return true;
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
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public File getPluginFile() {
        return plugin.getPluginFile();
    }

    @Override
    public void broadcast(final String message) {
        getProxy().broadcast(message);
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceBungeeAPI.getAPI();
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }
}