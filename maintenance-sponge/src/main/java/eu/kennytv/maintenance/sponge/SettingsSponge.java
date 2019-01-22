package eu.kennytv.maintenance.sponge;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import org.spongepowered.api.Sponge;

public final class SettingsSponge extends Settings {

    SettingsSponge(final MaintenanceSpongePlugin plugin) {
        super(plugin);

        final ClientPingServerListener listener = new ClientPingServerListener(plugin, this);
        Sponge.getEventManager().registerListeners(plugin, listener);
        pingListener = listener;

        reloadConfigs();
    }

    @Override
    public String getColoredString(final String s) {
        return s;
    }

    @Override
    protected String getConfigName() {
        return "spigot-config.yml";
    }
}
