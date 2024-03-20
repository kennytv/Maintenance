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

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.api.event.proxy.ServerMaintenanceChangedEvent;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.proxy.runnable.SingleMaintenanceRunnable;
import eu.kennytv.maintenance.core.proxy.runnable.SingleMaintenanceScheduleRunnable;
import eu.kennytv.maintenance.core.proxy.util.ProfileLookup;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author kennytv
 * @since 3.0
 */
public abstract class MaintenanceProxyPlugin extends MaintenancePlugin implements MaintenanceProxy {
    private final Map<String, Task> serverTasks = new HashMap<>();
    protected SettingsProxy settingsProxy;

    protected MaintenanceProxyPlugin(final String version, final ServerType serverType) {
        super(version, serverType);
    }

    @Override
    public void disable() {
        super.disable();
        if (settingsProxy.getMySQL() != null) {
            settingsProxy.getMySQL().close();
        }
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        if (settingsProxy.hasMySQL()) {
            settingsProxy.setMaintenanceToSQL(maintenance);
        }
        super.setMaintenance(maintenance);
    }

    @Override
    public boolean isMaintenance(final Server server) {
        return settingsProxy.isMaintenance(server.getName());
    }

    @Override
    public boolean setMaintenanceToServer(final Server server, final boolean maintenance) {
        if (maintenance) {
            if (!settingsProxy.addMaintenanceServer(server.getName())) {
                return false;
            }
        } else if (!settingsProxy.removeMaintenanceServer(server.getName())) {
            return false;
        }

        serverActions(server, maintenance);

        for (final String command : (maintenance ? settingsProxy.getCommandsOnMaintenanceEnable(server) : settingsProxy.getCommandsOnMaintenanceDisable(server))) {
            try {
                executeConsoleCommand(command.replace("%SERVER%", server.getName()));
            } catch (final Exception e) {
                getLogger().severe("Error while executing extra maintenance " + (maintenance ? "enable" : "disable") + " command: " + command);
                e.printStackTrace();
            }
        }
        return true;
    }

    public void serverActions(final Server server, final boolean maintenance) {
        if (server == null) return;

        // Skip to the even fire for dummy servers
        if (server.isRegisteredServer()) {
            if (maintenance) {
                final Server fallback = settingsProxy.getFallbackServer();
                if (fallback == null) {
                    if (server.hasPlayers()) {
                        getLogger().warning("The set fallback could not be found! Instead kicking players from that server off the network!");
                    }
                }
                kickPlayers(server, fallback);
            } else {
                server.broadcast(settingsProxy.getMessage("singleMaintenanceDeactivated", "%SERVER%", server.getName()));
            }

            cancelSingleTask(server);
        }

        eventManager.callEvent(new ServerMaintenanceChangedEvent(server, maintenance));
    }

    @Override
    public boolean isServerTaskRunning(final Server server) {
        return serverTasks.containsKey(server.getName());
    }

    @Override
    public Set<String> getMaintenanceServers() {
        return Collections.unmodifiableSet(settingsProxy.getMaintenanceServers());
    }

    public void cancelSingleTask(final Server server) {
        final Task task = serverTasks.remove(server.getName());
        if (task != null) {
            task.cancel();
        }
    }

    public MaintenanceRunnableBase startSingleMaintenanceRunnable(final Server server, final long duration, final TimeUnit unit, final boolean enable) {
        final MaintenanceRunnableBase runnable = new SingleMaintenanceRunnable(this, settingsProxy, (int) unit.toSeconds(duration), enable, server);
        serverTasks.put(server.getName(), runnable.getTask());
        return runnable;
    }

    public MaintenanceRunnableBase scheduleSingleMaintenanceRunnable(final Server server, final long duration, final long maintenanceDuration, final TimeUnit unit) {
        final MaintenanceRunnableBase runnable = new SingleMaintenanceScheduleRunnable(this, settingsProxy,
                (int) unit.toSeconds(duration), (int) unit.toSeconds(maintenanceDuration), server);
        serverTasks.put(server.getName(), runnable.getTask());
        return runnable;
    }

    @Override
    @Nullable
    public List<String> getMaintenanceServersDump() {
        final List<String> list = new ArrayList<>();
        if (isMaintenance()) {
            list.add("global");
        }
        list.addAll(settingsProxy.getMaintenanceServers());
        return list.isEmpty() ? null : list;
    }

    @Override
    public MaintenanceProxyCommand getCommandManager() {
        return (MaintenanceProxyCommand) commandManager;
    }

    @Override
    protected void kickPlayers() {
        // Send players to waiting server is set
        if (settingsProxy.getWaitingServer() != null) {
            final Server waitingServer = getServer(settingsProxy.getWaitingServer());
            if (waitingServer != null) {
                kickPlayersTo(waitingServer);
                return;
            }
        }

        // If not set, kick players from proxy
        kickPlayersFromProxy();
    }

    @Blocking
    @Nullable
    protected ProfileLookup doUUIDLookup(final String name) throws IOException {
        final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        if (status == 404) {
            if (settingsProxy.isFallbackToOfflineUUID()) {
                // Use offline uuid
                return new ProfileLookup(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), name);
            } else {
                // Return null if profile not found
                return null;
            }
        }

        try (final InputStream in = connection.getInputStream()) {
            final String output = CharStreams.toString(new InputStreamReader(in));
            final JsonObject json = GSON.fromJson(output, JsonObject.class);

            final UUID uuid = fromStringUUIDWithoutDashes(json.getAsJsonPrimitive("id").getAsString());
            final String username = json.getAsJsonPrimitive("name").getAsString();
            return new ProfileLookup(uuid, username);
        }
    }

    private UUID fromStringUUIDWithoutDashes(String undashedUUID) {
        return UUID.fromString(
            undashedUUID.substring(0, 8) + "-" + undashedUUID.substring(8, 12) + "-" +
                undashedUUID.substring(12, 16) + "-" + undashedUUID.substring(16, 20) + "-" +
                undashedUUID.substring(20, 32)
        );
    }

    public SettingsProxy getSettingsProxy() {
        return settingsProxy;
    }

    @Nullable
    public abstract String getServerNameOf(SenderInfo sender);

    protected abstract void kickPlayers(Server server, Server fallback);

    protected abstract void kickPlayersTo(Server server);

    protected abstract void kickPlayersFromProxy();
}