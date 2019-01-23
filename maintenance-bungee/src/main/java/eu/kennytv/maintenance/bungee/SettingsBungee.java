/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.maintenance.bungee;

import eu.kennytv.lib.config.Configuration;
import eu.kennytv.lib.config.YamlConfiguration;
import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.mysql.MySQL;
import eu.kennytv.maintenance.core.Settings;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SettingsBungee extends Settings {
    private final String mySQLTable;
    private final String serverTable;
    private final String maintenanceQuery;
    private final String serverQuery;
    private final MySQL mySQL;

    private final MaintenanceBungeePlugin maintenancePlugin;
    private final MaintenanceBungeeBase plugin;
    private Set<String> maintenanceServers;
    private String fallbackServer;
    private Configuration spigotServers;

    private long millisecondsToCheck;
    private long lastMySQLCheck;
    private long lastServerCheck;

    SettingsBungee(final MaintenanceBungeePlugin maintenancePlugin, final MaintenanceBungeeBase plugin) {
        super(maintenancePlugin);
        this.maintenancePlugin = maintenancePlugin;
        this.plugin = plugin;

        final ProxyPingListener listener = new ProxyPingListener(plugin, this);
        plugin.getProxy().getPluginManager().registerListener(plugin, listener);
        pingListener = listener;

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

    @Override
    protected void reloadExtraConfigs() throws IOException {
        spigotServers = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "SpigotServers.yml"));
    }

    @Override
    protected void createExtraFiles() {
        createFile("SpigotServers.yml");
    }

    @Override
    protected String getConfigName() {
        return "bungee-config.yml";
    }

    @Override
    protected boolean updateExtraConfig() {
        // 2.3.1 mysql.update-interval
        if (!config.contains("mysql.update-interval")) {
            config.set("mysql.update-interval", 15);
            return true;
        }
        return false;
    }

    @Override
    protected void loadExtraSettings() {
        fallbackServer = spigotServers.getString("fallback", "hub");
        if (mySQL != null) {
            final long configValue = config.getInt("mysql.update-interval");
            // Even if set to 0, only check every 500 millis
            millisecondsToCheck = configValue > 0 ? configValue * 1000 : 500;
            lastMySQLCheck = 0;
            lastServerCheck = 0;
            maintenanceServers = loadMaintenanceServersFromSQL();
        } else {
            final List<String> list = spigotServers.getStringList("maintenance-on");
            maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
        }
    }

    @Override
    public boolean isMaintenance() {
        if (mySQL != null && System.currentTimeMillis() - lastMySQLCheck > millisecondsToCheck) {
            final boolean databaseValue = loadMaintenance();
            if (databaseValue != maintenance) {
                maintenancePlugin.serverActions(maintenance);
                maintenance = databaseValue;
            }
            lastMySQLCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    public boolean isMaintenance(final ServerInfo server) {
        if (mySQL != null && System.currentTimeMillis() - lastServerCheck > millisecondsToCheck) {
            final Set<String> databaseValue = loadMaintenanceServersFromSQL();
            if (!databaseValue.equals(maintenanceServers)) {
                maintenancePlugin.serverActions(server, maintenance);
                maintenanceServers = databaseValue;
            }
            lastServerCheck = System.currentTimeMillis();
        }
        return maintenanceServers.contains(server.getName());
    }

    void setMaintenanceToSQL(final boolean maintenance) {
        maintenancePlugin.async(() -> {
            final String s = String.valueOf(maintenance);
            mySQL.executeUpdate("INSERT INTO " + mySQLTable + " (setting, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?", "maintenance", s, s);
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
            lastServerCheck = System.currentTimeMillis();
        } else {
            if (!maintenanceServers.remove(server)) return false;
            saveServersToConfig();
        }
        return true;
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

    private void saveServersToConfig() {
        spigotServers.set("maintenance-on", new ArrayList<>(maintenanceServers));
        saveSpigotServers();
    }

    private void saveSpigotServers() {
        saveFile(spigotServers, "SpigotServers.yml");
    }

    public Set<String> getMaintenanceServers() {
        return maintenanceServers;
    }

    public String getFallbackServer() {
        return fallbackServer;
    }
}
