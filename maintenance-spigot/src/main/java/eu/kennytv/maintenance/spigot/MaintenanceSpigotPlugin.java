package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.spigot.MaintenanceSpigotAPI;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.spigot.command.MaintenanceSpigotCommand;
import eu.kennytv.maintenance.spigot.listener.PlayerLoginListener;
import eu.kennytv.maintenance.spigot.metrics.MetricsLite;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author KennyTV
 * @since 2.0
 */
public final class MaintenanceSpigotPlugin extends MaintenanceModePlugin {
    private final MaintenanceSpigotBase plugin;
    private final SettingsSpigot settings;

    MaintenanceSpigotPlugin(final MaintenanceSpigotBase plugin) {
        super("§8[§eMaintenanceSpigot§8] ", plugin.getDescription().getVersion(), ServerType.SPIGOT);

        this.plugin = plugin;
        settings = new SettingsSpigot(this, plugin);

        plugin.getLogger().info("Plugin by KennyTV");
        plugin.getLogger().info(getUpdateMessage());
        plugin.getCommand("maintenancespigot").setExecutor(new MaintenanceSpigotCommand(this, settings));

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerLoginListener(this, settings), plugin);

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (pm.isPluginEnabled(serverListPlus)) {
            serverListPlusHook = new ServerListPlusHook(((ServerListPlusPlugin) serverListPlus).getCore());
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpigotAPI.getAPI();
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.setToConfig("enable-maintenance-mode", maintenance);
        settings.saveConfig();

        if (isTaskRunning())
            cancelTask();

        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.kickPlayer(settings.getKickMessage().replace("%NEWLINE%", "\n")));
            getServer().broadcastMessage(settings.getMessage("maintenanceActivated"));
        } else
            getServer().broadcastMessage(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public int startMaintenanceRunnable(final Runnable runnable) {
        return getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 20);
    }

    @Override
    public void async(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void cancelTask() {
        getServer().getScheduler().cancelTask(taskId);
        runnable = null;
        taskId = 0;
    }

    @Override
    public void broadcast(final String message) {
        getServer().broadcastMessage(message);
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
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public Server getServer() {
        return plugin.getServer();
    }
}
