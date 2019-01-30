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

package eu.kennytv.maintenance.core.proxy;

import eu.kennytv.lib.config.Configuration;
import eu.kennytv.lib.config.YamlConfiguration;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.proxy.mysql.MySQL;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SettingsProxy extends Settings {
    private final MaintenanceProxyPlugin plugin;
    private Configuration spigotServers;
    private Set<String> maintenanceServers;
    private String fallbackServer;

    private String mySQLTable;
    private String serverTable;
    private String maintenanceQuery;
    private String serverQuery;
    private MySQL mySQL;

    private long millisecondsToCheck;
    private long lastMySQLCheck;
    private long lastServerCheck;

    public SettingsProxy(final MaintenanceProxyPlugin plugin) {
        super(plugin, "bungee-config.yml");
        this.plugin = plugin;
    }

    private void setupMySQL() throws Exception {
        plugin.getLogger().info("Trying to open database connection...");
        final Configuration mySQLSection = config.getSection("mysql");
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
        plugin.getLogger().info("Done!");
    }

    @Override
    protected void reloadExtraConfigs() throws IOException {
        spigotServers = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(super.plugin.getDataFolder(), "SpigotServers.yml"));
    }

    @Override
    protected void createExtraFiles() {
        createFile("SpigotServers.yml");
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
        // Open database connection if enabledand not already done
        if (mySQL == null && config.getBoolean("mysql.use-mysql")) {
            try {
                setupMySQL();
            } catch (final Exception e) {
                mySQL = null;
                plugin.getLogger().warning("Error while trying do open database connection!");
                e.printStackTrace();
            }
        }

        fallbackServer = spigotServers.getString("fallback", "hub");
        if (mySQL != null) {
            maintenanceServers = loadMaintenanceServersFromSQL();
            maintenance = loadMaintenance();

            final long configValue = config.getInt("mysql.update-interval");
            // Even if set to 0, only check every 500 millis
            millisecondsToCheck = configValue > 0 ? configValue * 1000 : 500;
            lastMySQLCheck = 0;
            lastServerCheck = 0;
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
                plugin.serverActions(maintenance);
                maintenance = databaseValue;
            }
            lastMySQLCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    public boolean isMaintenance(final Server server) {
        if (mySQL != null && System.currentTimeMillis() - lastServerCheck > millisecondsToCheck) {
            final Set<String> databaseValue = loadMaintenanceServersFromSQL();
            if (!databaseValue.equals(maintenanceServers)) {
                plugin.serverActions(server, maintenance);
                maintenanceServers = databaseValue;
            }
            lastServerCheck = System.currentTimeMillis();
        }
        return maintenanceServers.contains(server.getName());
    }

    void setMaintenanceToSQL(final boolean maintenance) {
        plugin.async(() -> {
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
            plugin.async(() -> mySQL.executeUpdate("INSERT INTO " + serverTable + " (server) VALUES (?)", server));
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
            plugin.async(() -> mySQL.executeUpdate("DELETE FROM " + serverTable + " WHERE server = ?", server));
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
