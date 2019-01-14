package eu.kennytv.maintenance.api.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.IMaintenanceBase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to get the {@link IMaintenance} instance for the Spigot version of the plugin.
 * <p>
 * Only access this class if you're running the plugin on a Spigot server!
 * </p>
 *
 * @author KennyTV
 * @since 2.5
 */
public final class MaintenanceSpigotAPI {

    /**
     * Returns API instance of the Maintenance plugin.
     *
     * @return {@link IMaintenance} instance
     * @throws IllegalArgumentException if using a custom (or broken?) version of the plugin, that can't be identified
     */
    public static IMaintenance getAPI() {
        final Plugin maintenance = Bukkit.getPluginManager().getPlugin("MaintenanceSpigot");
        if (maintenance == null)
            throw new IllegalArgumentException("Could not get instance of MaintenanceSpigot! Broken/custom build of the plugin?");
        return ((IMaintenanceBase) maintenance).getApi();
    }
}
