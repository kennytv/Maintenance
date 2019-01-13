package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.spigot.listener.ServerInfoPacketListener;
import eu.kennytv.maintenance.spigot.listener.ServerListPingListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class SettingsSpigot extends Settings {
    private final MaintenanceSpigotBase plugin;
    private final IPingListener pingListener;
    private FileConfiguration config;
    private FileConfiguration language;
    private FileConfiguration whitelist;

    SettingsSpigot(final MaintenanceSpigotPlugin pl, final MaintenanceSpigotBase plugin) {
        super(pl);
        this.plugin = plugin;

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

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        createFile("spigot-config.yml");
        createFile("WhitelistedPlayers.yml");
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
    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "spigot-config.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveWhitelistedPlayers() {
        try {
            whitelist.save(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloadConfigs() {
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "spigot-config.yml")), StandardCharsets.UTF_8));
            whitelist = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance files!", e);
        }

        loadSettings();
        createLanguageFile();

        try {
            language = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "language-" + getLanguage() + ".yml")), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance language file!", e);
        }
    }

    @Override
    public void loadExtraSettings() {
        whitelistedPlayers.clear();
        whitelist.getKeys(false).forEach(key -> whitelistedPlayers.put(UUID.fromString(key), whitelist.getString(key)));
    }

    @Override
    public void setWhitelist(final String uuid, final String s) {
        whitelist.set(uuid, s);
    }

    @Override
    public String getConfigString(final String path) {
        final String s = config.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The config is missing the following string: " + path);
            return "null";
        }
        return s;
    }

    @Override
    public String getMessage(final String path) {
        final String s = language.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return "null";
        }
        return getColoredString(s);
    }

    @Override
    public boolean getConfigBoolean(final String path) {
        return config.getBoolean(path);
    }

    @Override
    public List<Integer> getConfigIntList(final String path) {
        return config.getIntegerList(path);
    }

    @Override
    public List<String> getConfigList(final String path) {
        return config.getStringList(path);
    }

    @Override
    public void setToConfig(final String path, final Object var) {
        config.set(path, var);
    }

    @Override
    public boolean configContains(final String path) {
        return config.contains(path);
    }

    @Override
    public String getColoredString(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public boolean reloadMaintenanceIcon() {
        return pingListener.loadIcon();
    }
}
