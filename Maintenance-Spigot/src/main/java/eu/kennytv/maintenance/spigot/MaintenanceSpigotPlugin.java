package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.MaintenanceSpigotAPI;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.spigot.command.MaintenanceSpigotCommand;
import eu.kennytv.maintenance.spigot.listener.PlayerLoginListener;
import eu.kennytv.maintenance.spigot.metrics.MetricsLite;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * @author KennyTV
 * @since 2.0
 */
public final class MaintenanceSpigotPlugin extends MaintenanceModePlugin {
    private final MaintenanceSpigotBase plugin;
    private final SettingsSpigot settings;

    MaintenanceSpigotPlugin(final MaintenanceSpigotBase plugin) {
        super("§8[§eMaintenanceSpigot§8] ", plugin.getDescription().getVersion());

        this.plugin = plugin;
        settings = new SettingsSpigot(plugin);

        plugin.getLogger().info("Plugin by KennyTV");
        plugin.getCommand("maintenancespigot").setExecutor(new MaintenanceSpigotCommand(this, settings));

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerLoginListener(this, settings), plugin);

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
        settings.setMaintenance(maintenance);
        settings.setToConfig("enable-maintenance-mode", maintenance);
        settings.saveConfig();
        settings.reloadConfigs();

        if (isTaskRunning())
            cancelTask();

        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.kickPlayer(settings.getKickMessage().replace("%NEWLINE%", "\n")));
            getServer().broadcastMessage(settings.getMaintenanceActivated());
        } else
            getServer().broadcastMessage(settings.getMaintenanceDeactivated());
    }

    @Override
    public int schedule(final Runnable runnable) {
        return getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 1200);
    }

    @Override
    public void async(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void cancelTask() {
        getServer().getScheduler().cancelTask(taskId);
        setTaskId(0);
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
        getServer().broadcastMessage(message);
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpigotAPI.getAPI();
    }

    public MaintenanceSpigotBase getBase() {
        return plugin;
    }

    public Server getServer() {
        return plugin.getServer();
    }
}