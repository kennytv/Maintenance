package eu.kennytv.maintenance.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to get the {@link IMaintenance} instance for the Spigot/Bukkit version of the plugin.
 * <p>
 * This class is NOT available for the BungeeCord version!
 * </p>
 *
 * @author KennyTV
 * @since 2.1
 */
public final class MaintenanceSpigotAPI {

    /**
     * Returns API instance of IMaintenance.
     *
     * @return {@link IMaintenance} instance
     */
    public static IMaintenance getAPI() {
        final Plugin maintenance = Bukkit.getPluginManager().getPlugin("MaintenanceSpigot");
        if (maintenance == null)
            Bukkit.getLogger().warning("Could not get instance of MaintenanceSpigot!");

        return ((IMaintenanceBase) maintenance).getApi();
    }
}
