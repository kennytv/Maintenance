package eu.kennytv.maintenance.api.sponge;

import eu.kennytv.maintenance.api.IMaintenance;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

/**
 * Utility class to get the {@link IMaintenance} instance for the Sponge version of the plugin.
 * <p>
 * Only access this class if you're running the plugin on a Sponge server!
 * </p>
 *
 * @author KennyTV
 * @since 3.0
 */
public final class MaintenanceSpongeAPI {

    /**
     * Returns API instance of the Maintenance plugin.
     *
     * @return {@link IMaintenance} instance
     */
    public static IMaintenance getAPI() {
        final Optional<PluginContainer> container = Sponge.getPluginManager().getPlugin("MaintenanceSponge");
        if (!container.isPresent() || !container.get().getInstance().isPresent())
            throw new IllegalArgumentException("Could not get instance of MaintenanceSponge! Broken/custom build of the plugin?");
        return (IMaintenance) container.get().getInstance().get();
    }
}
