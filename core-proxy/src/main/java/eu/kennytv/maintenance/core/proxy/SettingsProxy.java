/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
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
import eu.kennytv.maintenance.core.proxy.redis.RedisHandler;
import eu.kennytv.maintenance.core.proxy.redis.RedisPacketReceiver;
import eu.kennytv.maintenance.core.proxy.redis.impl.MaintenanceAddServerPacket;
import eu.kennytv.maintenance.core.proxy.redis.impl.MaintenanceRemoveServerPacket;
import eu.kennytv.maintenance.core.proxy.redis.impl.MaintenanceUpdatePacket;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SettingsProxy extends Settings {

    public static String REDIS_CHANNEL = "MAINTENANCE:ALL";

    private final MaintenanceProxyPlugin proxyPlugin;
    private Set<String> maintenanceServers;
    private List<String> fallbackServers;
    private String waitingServer;
    private boolean fallbackToOfflineUUID;

    private Map<String, List<String>> commandsOnMaintenanceEnable;
    private Map<String, List<String>> commandsOnMaintenanceDisable;
    private RedisHandler redisHandler;

    private long millisecondsToCheck;
    private long lastRedisCheck;
    private long lastServerCheck;

    public SettingsProxy(final MaintenanceProxyPlugin plugin) {
        super(plugin);
        this.proxyPlugin = plugin;
    }

    private void setupRedis() {
        try {
            plugin.getLogger().info("Trying to open database connection... (also, you can simply ignore the SLF4J soft-warning if it shows up)");
            final ConfigSection section = config.getSection("redis");
            if (section == null) {
                plugin.getLogger().warning("Section missing: redis");
                return;
            }

            redisHandler = new RedisHandler(section.getString("redis-uri"));
            redisHandler.registerReceiver(new RedisPacketReceiver(REDIS_CHANNEL));

            plugin.getLogger().info("Connected to redis!");

            redisHandler.set("maintenance", String.valueOf(maintenance));
            redisHandler.setList("maintenance-servers", new ArrayList<>());

            plugin.getLogger().info("Creating base info on redis!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void loadExtraSettings() {
        // Open database connection if enabled and not already done
        if (!hasRedis() && config.getBoolean("redis.use-redis")) {
            try {
                setupRedis();
            } catch (final Exception e) {
                redisHandler = null;
                plugin.getLogger().warning("Error while trying do open database connection!");
                e.printStackTrace();
            }
        }

        final Object fallback = config.getObject("fallback");
        fallbackServers = fallback instanceof String ? Collections.singletonList((String) fallback) : config.getStringList("fallback", Collections.emptyList());

        waitingServer = config.getString("waiting-server", "");
        if (waitingServer.isEmpty() || waitingServer.equalsIgnoreCase("none")) {
            waitingServer = null;
        }
        fallbackToOfflineUUID = config.getBoolean("fallback-to-offline-uuid", false);

        commandsOnMaintenanceEnable = new HashMap<>();
        final ConfigSection enableCommandsSection = config.getSection("commands-on-single-maintenance-enable");
        for (final String key : enableCommandsSection.getKeys()) {
            commandsOnMaintenanceEnable.put(key.toLowerCase(Locale.ROOT), enableCommandsSection.getStringList(key));
        }

        commandsOnMaintenanceDisable = new HashMap<>();
        final ConfigSection disableCommandsSection = config.getSection("commands-on-single-maintenance-disable");
        for (final String key : disableCommandsSection.getKeys()) {
            commandsOnMaintenanceDisable.put(key.toLowerCase(Locale.ROOT), disableCommandsSection.getStringList(key));
        }

        if (hasRedis()) {
            maintenance = loadMaintenance();
            maintenanceServers = loadMaintenanceServersFromRedis();

            final long configValue = config.getInt("redis.update-interval");
            // Even if set to 0, only check every 500 millis
            millisecondsToCheck = configValue > 0 ? configValue * 1000 : 500;
            lastRedisCheck = System.currentTimeMillis();
            lastServerCheck = System.currentTimeMillis();
        } else {
            final List<String> list = config.getStringList("proxied-maintenance-servers");
            maintenanceServers = list == null ? new HashSet<>() : new HashSet<>(list);
        }
    }

    @Override
    public boolean isMaintenance() {
        if (hasRedis() && System.currentTimeMillis() - lastRedisCheck > millisecondsToCheck) {
            final boolean databaseValue = loadMaintenance();
            if (databaseValue != maintenance) {
                maintenance = databaseValue;
                plugin.serverActions(maintenance);
            }
            lastRedisCheck = System.currentTimeMillis();
        }
        return maintenance;
    }

    public boolean isMaintenance(final String serverName) {
        if (hasRedis() && System.currentTimeMillis() - lastServerCheck > millisecondsToCheck) {
            final Set<String> databaseValue = loadMaintenanceServersFromRedis();
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

    public Component getServerKickMessage(final String server) {
        String message = getLanguageStringOrNull("singleMaintenanceKicks." + server, "%SERVER%", server);
        if (message == null) {
            message = getLanguageString("singleMaintenanceKick", "%SERVER%", server);
        }
        return parse(plugin.replacePingVariables(message));
    }

    // Full = being kicked from the proxy, not just a proxied server
    public Component getFullServerKickMessage(final String server) {
        String message = getLanguageStringOrNull("singleMaintenanceKicksComplete." + server, "%SERVER%", server);
        if (message == null) {
            message = getLanguageString("singleMaintenanceKickComplete", "%SERVER%", server);
        }
        return parse(plugin.replacePingVariables(message));
    }

    public boolean hasRedis() {
        return redisHandler != null;
    }

    void setMaintenanceToRedis(final boolean maintenance) {
        redisHandler.sendPacket(new MaintenanceUpdatePacket(maintenance));
        redisHandler.set("maintenance", String.valueOf(maintenance));
        lastRedisCheck = System.currentTimeMillis();
    }

    boolean addMaintenanceServer(final String server) {
        if (hasRedis()) {
            maintenanceServers = loadMaintenanceServersFromRedis();
            if (!maintenanceServers.add(server)) return false;
            redisHandler.sendPacket(new MaintenanceAddServerPacket(server));
            redisHandler.setList("maintenance-servers", new ArrayList<>(maintenanceServers));
            lastServerCheck = System.currentTimeMillis();
        } else {
            if (!maintenanceServers.add(server)) return false;
            saveServersToConfig();
        }
        return true;
    }

    boolean removeMaintenanceServer(final String server) {
        if (hasRedis()) {
            maintenanceServers = loadMaintenanceServersFromRedis();
            if (!maintenanceServers.remove(server)) return false;
            redisHandler.sendPacket(new MaintenanceRemoveServerPacket(server));
            redisHandler.setList("maintenance-servers", new ArrayList<>(maintenanceServers));

            lastServerCheck = System.currentTimeMillis();
        } else {
            if (!maintenanceServers.remove(server)) return false;

            saveServersToConfig();
        }
        return true;
    }

    private Set<String> loadMaintenanceServersFromRedis() {
        return new HashSet<>(redisHandler.getList("maintenance-servers"));
    }

    private boolean loadMaintenance() {
        boolean maintenance;

        try {
            maintenance = Boolean.parseBoolean(redisHandler.get("maintenance"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return maintenance;
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

    public boolean isFallbackToOfflineUUID() {
        return fallbackToOfflineUUID;
    }

    public List<String> getCommandsOnMaintenanceEnable(final Server server) {
        final List<String> enableCommands = commandsOnMaintenanceEnable.getOrDefault("all", new ArrayList<>());
        final List<String> serverEnableCommands = commandsOnMaintenanceEnable.get(server.getName().toLowerCase(Locale.ROOT));
        if (serverEnableCommands != null) {
            enableCommands.addAll(serverEnableCommands);
        }
        return enableCommands;
    }

    public List<String> getCommandsOnMaintenanceDisable(final Server server) {
        final List<String> disableCommands = commandsOnMaintenanceDisable.getOrDefault("all", new ArrayList<>());
        final List<String> serverDisableCommands = commandsOnMaintenanceDisable.get(server.getName().toLowerCase(Locale.ROOT));
        if (serverDisableCommands != null) {
            disableCommands.addAll(serverDisableCommands);
        }
        return disableCommands;
    }

    @Nullable
    RedisHandler getRedisHandler() {
        return redisHandler;
    }
}
