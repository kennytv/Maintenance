package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.spigot.listener.ServerInfoPacketListener;
import eu.kennytv.maintenance.spigot.listener.ServerListPingListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public final class SettingsSpigot extends Settings {

    SettingsSpigot(final MaintenanceSpigotPlugin pl, final MaintenanceSpigotBase plugin) {
        super(pl);

        final PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("ProtocolLib") != null) {
            final ServerInfoPacketListener serverInfoListener = new ServerInfoPacketListener(plugin, this);
            pm.registerEvents(serverInfoListener, plugin);
            pingListener = serverInfoListener;
        } else {
            final ServerListPingListener listener = new ServerListPingListener(plugin, this);
            pm.registerEvents(listener, plugin);
            pingListener = listener;
        }

        reloadConfigs();
    }

    @Override
    public boolean updateExtraConfig() {
        // Remove MySQL part from default config
        /*if (configContains("mysql")) {
            setToConfig("mysql", null);
            return true;
        }*/
        return false;
    }

    @Override
    public String getColoredString(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    protected String getConfigName() {
        return "spigot-config.yml";
    }
}
