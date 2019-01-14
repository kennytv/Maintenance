package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;

public final class GameReloadListener {
    private final MaintenanceSpongePlugin plugin;

    public GameReloadListener(final MaintenanceSpongePlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void reload(final GameReloadEvent event) {
        plugin.getSettings().reloadConfigs();
        plugin.getLogger().info("Reloaded config files!");
    }
}
