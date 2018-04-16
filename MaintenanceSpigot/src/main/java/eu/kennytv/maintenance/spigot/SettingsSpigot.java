package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.core.Settings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public final class SettingsSpigot extends Settings {
    private final MaintenanceSpigotBase plugin;
    private FileConfiguration config;
    private FileConfiguration whitelist;

    SettingsSpigot(final MaintenanceSpigotBase plugin) {
        this.plugin = plugin;
        createFiles();
        reloadConfigs();
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
        final File file = new File(plugin.getDataFolder(), "spigot-config.yml");
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF8"));
            whitelist = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load spigot-config.yml!", e);
        }

        loadSettings();
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
    public boolean createFiles() {
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        final File file = new File(plugin.getDataFolder(), "spigot-config.yml");
        if (!file.exists()) {
            try (final InputStream in = plugin.getResource("spigot-config.yml")) {
                Files.copy(in, file.toPath());
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create spigot-config.yml file for Maintenance!", e);
            }
        }

        final File whitelistFile = new File(plugin.getDataFolder(), "WhitelistedPlayers.yml");
        if (!whitelistFile.exists()) {
            try (final InputStream in = plugin.getResource("WhitelistedPlayers.yml")) {
                Files.copy(in, whitelistFile.toPath());
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create WhitelistedPlayers.yml file for Maintenance!", e);
            }
        }
        return true;
    }

    @Override
    public String getConfigString(final String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }

    @Override
    public boolean getConfigBoolean(final String path) {
        return config.getBoolean(path);
    }

    @Override
    public List<Integer> getBroadcastIntervallList() {
        return config.getIntegerList("timer-broadcasts-for-minutes");
    }

    @Override
    public void setToConfig(final String path, final Object var) {
        config.set(path, var);
    }
}
