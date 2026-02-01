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

import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.proxy.redis.RedisHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public final class SettingsProxy extends Settings {

    private Set<String> maintenanceServers = new HashSet<>();
    private List<String> fallbackServers;
    private String waitingServer;
    private boolean fallbackToOfflineUUID;

    private Map<String, List<String>> commandsOnMaintenanceEnable;
    private Map<String, List<String>> commandsOnMaintenanceDisable;

    private RedisHandler redisHandler;

    public SettingsProxy(final MaintenanceProxyPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void loadExtraSettings() {
        // Open Redis connection if enabled and not already done
        final ConfigSection redisSection = config.getSection("redis");
        if (!hasRedis() && redisSection.getBoolean("enabled")) {
            connectToRedis(redisSection.getString("uri"));
        }

        final Object fallback = config.getObject("fallback");
        fallbackServers = fallback instanceof String s ? Collections.singletonList(s) : config.getStringList("fallback", Collections.emptyList());

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

        if (!hasRedis()) {
            final List<String> list = config.getStringList("proxied-maintenance-servers");
            maintenanceServers = new HashSet<>(list);
        }
    }

    private void connectToRedis(final String redisUri) {
        redisHandler = new RedisHandler((MaintenanceProxyPlugin) plugin, this);
        try {
            redisHandler.setup(redisUri);
        } catch (final Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to open Redis connection!", e);
            e.printStackTrace();
            redisHandler.close();
            return;
        }

        maintenance = redisHandler.loadMaintenanceStatus();
        maintenanceServers = redisHandler.loadMaintenanceServers();
    }

    public boolean isMaintenance(final String serverName) {
        return maintenanceServers.contains(serverName);
    }

    public Component getServerKickMessage(final String server) {
        String message = getLanguageStringOrNull("singleMaintenanceKicks." + server, "%SERVER%", server);
        if (message == null) {
            message = getLanguageString("singleMaintenanceKick", "%SERVER%", server);
        }
        return parse(plugin.replacePingVariables(message));
    }

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

    @Override
    protected void loadWhitelistedPlayers() {
        if (hasRedis()) {
            redisHandler.loadPlayers(getWhitelistedPlayers());
        } else {
            super.loadWhitelistedPlayers();
        }
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        super.setMaintenance(maintenance);
        if (hasRedis()) {
            redisHandler.set(maintenance);
        }
    }

    public void setMaintenanceDirect(final boolean maintenance) {
        super.setMaintenance(maintenance);
    }

    @Override
    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        if (super.addWhitelistedPlayer(uuid, name)) {
            if (hasRedis()) {
                redisHandler.addPlayer(uuid, name);
            }
            return true;
        }
        return false;
    }

    public boolean addWhitelistedPlayerDirect(final UUID uuid, final String name) {
        return super.addWhitelistedPlayer(uuid, name);
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (super.removeWhitelistedPlayer(uuid)) {
            if (hasRedis()) {
                redisHandler.removePlayer(uuid);
            }
            return true;
        }
        return false;
    }

    public boolean removeWhitelistedPlayerDirect(final UUID uuid) {
        return super.removeWhitelistedPlayer(uuid);
    }

    boolean addMaintenanceServer(final String server) {
        if (hasRedis()) {
            if (redisHandler.addServer(server)) {
                maintenanceServers.add(server);
                return true;
            }
        } else if (maintenanceServers.add(server)) {
            saveServersToConfig();
            return true;
        }
        return false;
    }

    boolean removeMaintenanceServer(final String server) {
        if (hasRedis()) {
            if (redisHandler.removeServer(server)) {
                maintenanceServers.remove(server);
                return true;
            }
        } else if (maintenanceServers.remove(server)) {
            saveServersToConfig();
            return true;
        }
        return false;
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
            final Server server = ((MaintenanceProxy) plugin).getServer(fallbackServer);
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

    public @Nullable RedisHandler redisHandler() {
        return redisHandler;
    }
}