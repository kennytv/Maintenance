/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2021 KennyTV (https://github.com/KennyTV)
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

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.proxy.mysql.MySQL;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SettingsProxy extends Settings {
    private final MaintenanceProxyPlugin proxyPlugin;
    private Set<String> maintenanceServers;
    private List<String> fallbackServers;
    private String waitingServer;

    private String mySQLTable;
    private String serverTable;
    private String maintenanceQuery;
    private String serverQuery;
    private MySQL mySQL;

    private long millisecondsToCheck;
    private long lastMySQLCheck;
    private long lastServerCheck;

    public SettingsProxy(final MaintenanceProxyPlugin plugin) {
        super(plugin);
        this.proxyPlugin = plugin;
    }

    private void setupMySQL() throws Exception {
        plugin.getLogger().info("Trying to open database connection... (also, you can simply ignore the SLF4J soft-warning if it shows up)");
        final ConfigSection section = config.getSection("mysql");
        if (section == null) {
            plugin.getLogger().warning("Section missing: mysql");
            return;
        }

        mySQL = new MySQL(plugin.getLogger(),
                section.getString("host"),
                section.getInt("port"),
                section.getString("username"),
                section.getString("password"),
                section.getString("database"),
                section.getBoolean("use-ssl", true));

        // Varchar as the value regarding the possibility of saving stuff like the motd as well in future updates
        mySQLTable = section.getString("table", "maintenance_settings");
        serverTable = section.getString("servertable", "maintenance_servers");
        mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS " + mySQLTable + " (setting VARCHAR(16) PRIMARY KEY, value VARCHAR(255))");
        mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS " + serverTable + " (server VARCHAR(64) PRIMARY KEY)");
        maintenanceQuery = "SELECT * FROM " + mySQLTable + " WHERE setting = ?";
        serverQuery = "SELECT * FROM " + serverTable;
        plugin.getLogger().info("Done!");
    }

    @Override
    protected void loadExtraSettings() {
        // Open database connection if enabled and not already done
        if (!hasMySQL() && config.getBoolean("mysql.use-mysql")) {
            try {
                setupMySQL();
            } catch (final Exception e) {
                mySQL = null;
                plugin.getLogger().warning("Error while trying do open database connection!");
                e.printStackTrace();
            }
        }

        final Object fallback = config.getObject("fallback");
        fallbackServers = fallback instanceof String ? Collections.singletonList((String) fallback) : config.getStringList("fallback");

        waitingServer = config.getString("waiting-server", "");
        if (waitingServer.isEmpty() || waitingServer.equalsIgnoreCase("none")) {
            waitingServer = null;
        }

        if (hasMySQL()) {
            maintenance = loadMaintenance();
            maintenanceServers = loadMaintenanceServersFromSQL();

            final long configValue = config.getInt("mysql.update-interval");
            // Even if set to 0, only check every 500 millis
            millisecondsToCheck = configValue > 0 ? configValue * 1000 : 500;
            lastMySQLCheck = System.currentTimeMillis();
            lastServerCheck = System.currentTimeMillis();
        } else {
            final List<String> list = config.getStringList("proxied-maintenance-servers");
            maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
        }
    }

    @Override
    public boolean isMaintenance() {
        if (hasMySQL() && System.currentTimeMillis() - lastMySQLCheck > millisecondsToCheck) {
            final boolean databaseValue = loadMaintenance();
            if (databaseValue != maintenance) {
                maintenance = databaseValue;
                plugin.serverActions(maintenance);
            }
            lastMySQLCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    public boolean isMaintenance(final String serverName) {
        if (hasMySQL() && System.currentTimeMillis() - lastServerCheck > millisecondsToCheck) {
            final Set<String> databaseValue = loadMaintenanceServersFromSQL();
            if (!maintenanceServers.equals(databaseValue)) {
                // Enable maintenance on yet unlisted servers
                for (final String s : databaseValue) {
                    if (!maintenanceServers.contains(s)) {
                        proxyPlugin.serverActions(proxyPlugin.getServer(s), true);
                    }
                }
                // Disable maintenance on now unlisted servers
                for (final String s : maintenanceServers) {
                    if (!databaseValue.contains(s)) {
                        proxyPlugin.serverActions(proxyPlugin.getServer(s), false);
                    }
                }
                maintenanceServers = databaseValue;
            }
            lastServerCheck = System.currentTimeMillis();
        }
        return maintenanceServers.contains(serverName);
    }

    public String getServerKickMessage(final String server) {
        String message = language.getString("singleMaintenanceKicks." + server);
        if (message == null) {
            message = getMessage("singleMaintenanceKick");
        }
        return plugin.replacePingVariables(message).replace("%SERVER%", server);
    }

    // Full = being kicked from the proxy, not just a proxied server
    public String getFullServerKickMessage(final String server) {
        String message = language.getString("singleMaintenanceKicksComplete." + server);
        if (message == null) {
            message = getMessage("singleMaintenanceKickComplete");
        }
        return plugin.replacePingVariables(message).replace("%SERVER%", server);
    }

    public boolean hasMySQL() {
        return mySQL != null;
    }

    void setMaintenanceToSQL(final boolean maintenance) {
        plugin.async(() -> {
            final String s = String.valueOf(maintenance);
            mySQL.executeUpdate("INSERT INTO " + mySQLTable + " (setting, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?", "maintenance", s, s);
            lastMySQLCheck = System.currentTimeMillis();
        });
    }

    boolean addMaintenanceServer(final String server) {
        if (hasMySQL()) {
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
        if (hasMySQL()) {
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
                plugin.getLogger().warning("An error occured while trying to get the maintenance value from the database!");
                e.printStackTrace();
            }
        }, "maintenance");
        return databaseValue[0];
    }

    private void saveServersToConfig() {
        config.set("proxied-maintenance-servers", new ArrayList<>(maintenanceServers));
        saveConfig();
    }

    public Set<String> getMaintenanceServers() {
        return maintenanceServers;
    }

    @Nullable
    public Server getFallbackServer() {
        for (final String fallbackServer : fallbackServers) {
            final Server server = proxyPlugin.getServer(fallbackServer);
            if (server != null && !isMaintenance(server.getName())) {
                return server;
            }
        }
        return null;
    }

    @Nullable
    public String getWaitingServer() {
        return waitingServer;
    }

    @Nullable
    MySQL getMySQL() {
        return mySQL;
    }
}
