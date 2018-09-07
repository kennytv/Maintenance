package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.MaintenanceBungeeAPI;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommand;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommandBase;
import eu.kennytv.maintenance.bungee.listener.PostLoginListener;
import eu.kennytv.maintenance.bungee.metrics.MetricsLite;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeePlugin extends MaintenanceModePlugin {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super("§8[§eMaintenanceBungee§8] ", plugin.getDescription().getVersion());

        this.plugin = plugin;
        plugin.getLogger().info("Plugin by KennyTV");

        settings = new SettingsBungee(this, plugin);

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new PostLoginListener(this, settings));
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
                    .forEach(p -> p.disconnect(settings.getMessage("kickmessage").replace("%NEWLINE%", "\n")));
            getProxy().broadcast(settings.getMessage("maintenanceActivated"));
        } else
            getProxy().broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public int startMaintenanceRunnable(final Runnable runnable) {
        return getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId();
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