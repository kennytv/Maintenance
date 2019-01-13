package eu.kennytv.maintenance.sponge;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import org.spongepowered.api.Sponge;

import java.util.List;

public final class SettingsSponge extends Settings {
    private final MaintenanceSpongePlugin plugin;
    private final IPingListener pingListener;

    SettingsSponge(final MaintenanceSpongePlugin plugin) {
        super(plugin);
        this.plugin = plugin;

        final ClientPingServerListener listener = new ClientPingServerListener(plugin, this);
        Sponge.getEventManager().registerListeners(plugin, listener);
        pingListener = listener;

        //createFile("spigot-config.yml");
        //createFile("WhitelistedPlayers.yml");
        reloadConfigs();
    }

    @Override
    public void saveWhitelistedPlayers() {

    }

    @Override
    public String getConfigString(final String path) {
        return null;
    }

    @Override
    public String getMessage(final String path) {
        return null;
    }

    @Override
    public boolean getConfigBoolean(final String path) {
        return false;
    }

    @Override
    public List<Integer> getConfigIntList(final String path) {
        return null;
    }

    @Override
    public List<String> getConfigList(final String path) {
        return null;
    }

    @Override
    public void loadExtraSettings() {

    }

    @Override
    public void setWhitelist(final String uuid, final String s) {

    }

    @Override
    public void saveConfig() {

    }

    @Override
    public void setToConfig(final String path, final Object var) {

    }

    @Override
    public boolean configContains(final String path) {
        return false;
    }

    @Override
    public String getColoredString(final String s) {
        return null;
    }

    @Override
    public boolean reloadMaintenanceIcon() {
        return false;
    }

    @Override
    public void reloadConfigs() {

    }
}
