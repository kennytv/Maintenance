package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.mysql.MySQL;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.IPingListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

public final class SettingsBungee extends Settings {
    private final String mySQLTable;
    private final String serverTable;
    private final String maintenanceQuery;
    private final String serverQuery;
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
    private long lastServerCheck;

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
            mySQLTable = mySQLSection.getString("table", "maintenance_settings");
            serverTable = mySQLSection.getString("servertable", "maintenance_servers");
            mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS " + mySQLTable + " (setting VARCHAR(16) PRIMARY KEY, value VARCHAR(255))");
            mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS " + serverTable + " (server VARCHAR(64) PRIMARY KEY)");
            maintenanceQuery = "SELECT * FROM " + mySQLTable + " WHERE setting = ?";
            serverQuery = "SELECT * FROM " + serverTable;
            maintenance = loadMaintenance();
            plugin.getLogger().info("Done!");
        } else {
            mySQL = null;
            mySQLTable = null;
            maintenanceQuery = null;
            serverTable = null;
            serverQuery = null;
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
            spigotServers = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "SpigotServers.yml"));
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
            lastServerCheck = 0;
            maintenanceServers = loadMaintenanceServersFromSQL();
        } else {
            final List<String> list = spigotServers.getStringList("maintenance-on");
            maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
        }
    }

    private Set<String> loadMaintenanceServersFromSQL() {
        final Set<String> maintenanceServers = new HashSet<>();
        mySQL.executeQuery(serverQuery, rs -> {
            try {
                while (rs.next()) {
                    maintenanceServers.add(rs.getString("server"));
                }
            } catch (final SQLException e) {
                plugin.getLogger().warning("An error occured while trying to get the list of single servers with maintenance!");
                e.printStackTrace();
            }
        });
        return maintenanceServers;
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
            final boolean databaseValue = loadMaintenance();
            if (databaseValue != maintenance) {
                maintenancePlugin.serverActions(maintenance);
                maintenance = databaseValue;
            }
            if (millisecondsToCheck != -1)
                lastMySQLCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    @Override
    public boolean reloadMaintenanceIcon() {
        return pingListener.loadIcon();
    }

    private boolean loadMaintenance() {
        final boolean[] databaseValue = {false};
        mySQL.executeQuery(maintenanceQuery, rs -> {
            try {
                if (rs.next()) {
                    databaseValue[0] = Boolean.parseBoolean(rs.getString("value"));
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }, "maintenance");
        return databaseValue[0];
    }

    public boolean isMaintenance(final ServerInfo server) {
        if (mySQL != null && (millisecondsToCheck == -1 || System.currentTimeMillis() - lastServerCheck > millisecondsToCheck)) {
            final Set<String> databaseValue = loadMaintenanceServersFromSQL();
            if (!databaseValue.equals(maintenanceServers)) {
                maintenancePlugin.serverActions(server, maintenance);
                maintenanceServers = databaseValue;
            }
            if (millisecondsToCheck != -1)
                lastServerCheck = System.currentTimeMillis();
        }
        return maintenanceServers.contains(server.getName());
    }

    void setMaintenanceToSQL(final boolean maintenance) {
        maintenancePlugin.async(() -> {
            final String s = String.valueOf(maintenance);
            mySQL.executeUpdate("INSERT INTO " + mySQLTable + " (setting, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?", "maintenance", s, s);
            if (millisecondsToCheck != -1)
                lastMySQLCheck = System.currentTimeMillis();
        });
        this.maintenance = maintenance;
    }

    MySQL getMySQL() {
        return mySQL;
    }

    boolean addMaintenanceServer(final String server) {
        if (mySQL != null) {
            maintenanceServers = loadMaintenanceServersFromSQL();
            if (!maintenanceServers.add(server)) return false;
            maintenancePlugin.async(() -> mySQL.executeUpdate("INSERT INTO " + serverTable + " (server) VALUES (?)", server));
            if (millisecondsToCheck != -1)
                lastServerCheck = System.currentTimeMillis();
        } else {
            if (!maintenanceServers.add(server)) return false;
            saveServersToConfig();
        }
        return true;
    }

    boolean removeMaintenanceServer(final String server) {
        if (mySQL != null) {
            maintenanceServers = loadMaintenanceServersFromSQL();
            if (!maintenanceServers.remove(server)) return false;
            maintenancePlugin.async(() -> mySQL.executeUpdate("DELETE FROM " + serverTable + " WHERE server = ?", server));
            if (millisecondsToCheck != -1)
                lastServerCheck = System.currentTimeMillis();
        } else {
            if (!maintenanceServers.remove(server)) return false;
            saveServersToConfig();
        }
        return true;
    }

    public void saveServersToConfig() {
        spigotServers.set("maintenance-on", new ArrayList<>(maintenanceServers));
        saveSpigotServers();
    }

    public Set<String> getMaintenanceServers() {
        return maintenanceServers;
    }

    public String getFallbackServer() {
        return fallbackServer;
    }
}
