package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.mysql.MySQL;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.IPingListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SettingsBungee extends Settings {
    private final String updateQuery;
    private final String maintenanceQuery;
    //private final String serversQuery;
    private final MySQL mySQL;

    private final MaintenanceBungeePlugin maintenancePlugin;
    private final MaintenanceBungeeBase plugin;
    private final IPingListener pingListener;
    private Set<String> maintenanceServers;
    private Configuration config;
    private Configuration language;
    private Configuration whitelist;
    private Configuration spigotServers;
    private String fallbackServer;

    private long millisecondsToCheck;
    private long lastMySQLCheck;

    SettingsBungee(final MaintenanceBungeePlugin maintenancePlugin, final MaintenanceBungeeBase plugin) {
        super(maintenancePlugin);
        this.maintenancePlugin = maintenancePlugin;
        this.plugin = plugin;

        final PluginManager pm = plugin.getProxy().getPluginManager();
        final ProxyPingListener listener = new ProxyPingListener(plugin, this);
        pm.registerListener(plugin, listener);
        pingListener = listener;

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        createFile("bungee-config.yml");
        createFile("WhitelistedPlayers.yml");
        createFile("SpigotServers.yml");
        reloadConfigs();

        final Configuration mySQLSection = config.getSection("mysql");
        if (mySQLSection.getBoolean("use-mysql", false)) {
            plugin.getLogger().info("Trying to open database connection...");
            mySQL = new MySQL(mySQLSection.getString("host"),
                    mySQLSection.getInt("port"),
                    mySQLSection.getString("username"),
                    mySQLSection.getString("password"),
                    mySQLSection.getString("database"));

            // Varchar as the value regarding the possibility of saving stuff like the motd as well in future updates
            final String mySQLTable = mySQLSection.getString("table");
            mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS " + mySQLTable + " (setting VARCHAR(16) PRIMARY KEY, value VARCHAR(255))");
            updateQuery = "INSERT INTO " + mySQLTable + " (setting, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?";
            maintenanceQuery = "SELECT * FROM " + mySQLTable + " WHERE setting = ?";
            //serversQuery = "INSERT INTO " + serversTable + " (setting, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?";
            plugin.getLogger().info("Done!");
        } else {
            mySQL = null;
            updateQuery = null;
            maintenanceQuery = null;
            //serversQuery = null;
        }
    }

    private void createFile(final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try (final InputStream in = plugin.getResourceAsStream(name)) {
                Files.copy(in, file.toPath());
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create " + name + " file for MaintenanceBungee", e);
            }
        }
    }

    @Override
    public boolean updateExtraConfig() {
        // 2.3.1 mysql.update-interval
        if (!configContains("mysql.update-interval")) {
            setToConfig("mysql.update-interval", 15);
            return true;
        }
        return false;
    }

    @Override
    public void reloadConfigs() {
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "bungee-config.yml")), StandardCharsets.UTF_8));
            whitelist = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
            spigotServers = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "spigotServers.yml"));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance files!", e);
        }

        loadSettings();
        createAndLoadLanguageFile();
    }

    private void createAndLoadLanguageFile() {
        final File file = new File(plugin.getDataFolder(), "language-" + getLanguage() + ".yml");
        if (!file.exists()) {
            try (final InputStream in = plugin.getResourceAsStream("language-" + getLanguage() + ".yml")) {
                Files.copy(in, file.toPath());
            } catch (final IOException | NullPointerException e) {
                plugin.getLogger().warning("Unable to provide language " + getLanguage());
                if (!languageName.equals("en")) {
                    plugin.getLogger().warning("Falling back to default language: en");
                    languageName = "en";
                    createAndLoadLanguageFile();
                    return;
                }
            }
        }
        try {
            language = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "language-" + getLanguage() + ".yml")), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance language file!", e);
        }
    }

    @Override
    public void saveConfig() {
        final File file = new File(plugin.getDataFolder(), "bungee-config.yml");
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save bungee-config.yml!", e);
        }
    }

    @Override
    public void saveWhitelistedPlayers() {
        saveFile(whitelist, "WhitelistedPlayers.yml");
    }

    public void saveSpigotServers() {
        saveFile(spigotServers, "SpigotServers.yml");
    }

    private void saveFile(final Configuration config, final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, file);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save " + name + "!", e);
        }
    }

    @Override
    public void loadExtraSettings() {
        whitelistedPlayers.clear();
        whitelist.getKeys().forEach(key -> whitelistedPlayers.put(UUID.fromString(key), whitelist.getString(key)));
        fallbackServer = spigotServers.getString("fallback", "hub");
        if (mySQL != null) {
            final long configValue = config.getInt("mysql.update-interval");
            millisecondsToCheck = configValue > 0 ? configValue * 1000 : -1;
            lastMySQLCheck = 0;
        }/* else {
            final List<String> list = spigotServers.getStringList("maintenance-on");
            maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
        }*/

        final List<String> list = spigotServers.getStringList("maintenance-on");
        maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
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
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public boolean getConfigBoolean(final String path) {
        return config.getBoolean(path);
    }

    @Override
    public List<Integer> getConfigIntList(final String path) {
        return config.getIntList(path);
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
    public boolean isMaintenance() {
        if (mySQL != null && (millisecondsToCheck == -1 || System.currentTimeMillis() - lastMySQLCheck > millisecondsToCheck)) {
            mySQL.executeQuery(maintenanceQuery, rs -> {
                try {
                    if (rs.next()) {
                        final boolean databaseValue = Boolean.parseBoolean(rs.getString("value"));
                        if (databaseValue != maintenance)
                            maintenancePlugin.serverActions(maintenance);

                        maintenance = databaseValue;
                    }

                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }, "maintenance");
            if (millisecondsToCheck != -1)
                lastMySQLCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    @Override
    public boolean reloadMaintenanceIcon() {
        return pingListener.loadIcon();
    }

    public void setMaintenanceToSQL(final boolean maintenance) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            final String s = String.valueOf(maintenance);
            mySQL.executeUpdate(updateQuery, "maintenance", s, s);
            if (millisecondsToCheck != -1)
                lastMySQLCheck = System.currentTimeMillis();
        });
        this.maintenance = maintenance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public Set<String> getMaintenanceServers() {
        return maintenanceServers;
    }

    public void saveServersToConfig() {
        spigotServers.set("maintenance-on", maintenanceServers);
        saveSpigotServers();
    }

    public String getFallbackServer() {
        return fallbackServer;
    }
}
