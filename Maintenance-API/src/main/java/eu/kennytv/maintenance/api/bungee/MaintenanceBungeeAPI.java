package eu.kennytv.maintenance.api.bungee;

import eu.kennytv.maintenance.api.IMaintenanceBase;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Utility class to get the {@link IMaintenanceBungee} instance for the BungeeCord version of the plugin.
 * <p>
 * Only access this class if you're running the plugin on a BungeeCord server!
 * </p>
 *
 * @author KennyTV
 * @since 2.5
 */
public final class MaintenanceBungeeAPI {

    /**
     * Returns API instance of IMaintenance.
     *
     * @return {@link IMaintenanceBungee} instance
     */
    public static IMaintenanceBungee getAPI() {
        final Plugin maintenance = ProxyServer.getInstance().getPluginManager().getPlugin("MaintenanceBungee");
        if (maintenance == null)
            ProxyServer.getInstance().getLogger().warning("Could not get instance of MaintenanceBungee!");
        return (IMaintenanceBungee) ((IMaintenanceBase) maintenance).getApi();
    }
}
