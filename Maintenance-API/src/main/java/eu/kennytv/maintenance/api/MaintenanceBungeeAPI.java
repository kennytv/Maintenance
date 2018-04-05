package eu.kennytv.maintenance.api;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Utility class to get the {@link IMaintenance} instance for the BungeeCord version of the plugin.
 * <p>
 * This class is NOT available for the Spigot version!
 * </p>
 *
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeeAPI {

    /**
     * Returns API instance of IMaintenance.
     *
     * @return {@link IMaintenance} instance
     */
    public static IMaintenance getAPI() {
        final Plugin maintenance = ProxyServer.getInstance().getPluginManager().getPlugin("MaintenanceBungee");
        if (maintenance == null)
            ProxyServer.getInstance().getLogger().warning("Could not get instance of MaintenanceBungee!");

        return (IMaintenance) maintenance;
    }
}
