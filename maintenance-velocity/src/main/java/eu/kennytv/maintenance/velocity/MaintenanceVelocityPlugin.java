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

package eu.kennytv.maintenance.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ProxyPingEvent;
import com.velocitypowered.api.event.lifecycle.ProxyInitializeEvent;
import com.velocitypowered.api.event.lifecycle.ProxyReloadEvent;
import com.velocitypowered.api.event.lifecycle.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.connection.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.Favicon;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.util.ProfileLookup;
import eu.kennytv.maintenance.core.proxy.util.ProxyOfflineSenderInfo;
import eu.kennytv.maintenance.core.util.MaintenanceVersion;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.velocity.command.MaintenanceVelocityCommand;
import eu.kennytv.maintenance.velocity.listener.ProxyPingListener;
import eu.kennytv.maintenance.velocity.listener.ServerConnectListener;
import eu.kennytv.maintenance.velocity.util.LoggerWrapper;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import eu.kennytv.maintenance.velocity.util.VelocityServer;
import eu.kennytv.maintenance.velocity.util.VelocityTask;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author KennyTV
 * @since 3.0
 */
@Plugin(id = "maintenance", name = "Maintenance", version = MaintenanceVersion.VERSION, authors = "KennyTV",
        description = "Enable maintenance mode with a custom maintenance motd and icon.", url = "https://forums.velocitypowered.com/t/maintenance/129",
        dependencies = @Dependency(id = "serverlistplus", optional = true))
public final class MaintenanceVelocityPlugin extends MaintenanceProxyPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final File dataFolder;
    private Favicon favicon;

    @Inject
    public MaintenanceVelocityPlugin(final ProxyServer server, final org.slf4j.Logger logger, @DataDirectory final Path folder) {
        super(MaintenanceVersion.VERSION, ServerType.VELOCITY);
        this.server = server;
        this.logger = new LoggerWrapper(logger);
        this.dataFolder = folder.toFile();
    }

    @Subscribe
    public void onEnable(final ProxyInitializeEvent event) {
        settingsProxy = new SettingsProxy(this);
        settings = settingsProxy;

        sendEnableMessage();

        final MaintenanceVelocityCommand command = new MaintenanceVelocityCommand(this, settingsProxy);
        commandManager = command;
        server.commandManager().register(server.commandManager().createMetaBuilder("maintenance").aliases("maintenancevelocity", "mt").build(), command);

        final EventManager em = server.eventManager();
        em.register(this, ProxyPingEvent.class, PostOrder.LAST, new ProxyPingListener(this, settingsProxy));
        em.register(this, new ServerConnectListener(this, settingsProxy));

        continueLastEndtimer();

        // ServerListPlus integration
        PluginContainer serverListPlus = server.pluginManager().getPlugin("serverlistplus");
        if (serverListPlus != null && serverListPlus.instance() != null) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus.instance());
            if (settings.isEnablePingMessages()) {
                serverListPlusHook.setEnabled(!settingsProxy.isMaintenance());
            }
            logger.info("Enabled ServerListPlus integration!");
        }
    }

    @Subscribe
    public void proxyReload(final ProxyReloadEvent event) {
        settingsProxy.reloadConfigs();
        logger.info("Reloaded config files!");
    }

    @Subscribe
    public void onDisable(final ProxyShutdownEvent event) {
        disable();
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        final TextComponent component = translate(getPrefix()).append(translate("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/"));
        final TextComponent clickText = translate(" §7§l§o(CLICK ME)")
                .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/maintenance.40699/"))
                .hoverEvent(HoverEvent.showText(translate("§aDownload the latest version")));
        ((VelocitySenderInfo) sender).sendMessage(component.append(clickText));
    }

    public boolean isMaintenance(final RegisteredServer serverInfo) {
        return settingsProxy.isMaintenance(serverInfo.serverInfo().name());
    }

    @Override
    protected void kickPlayersFromProxy() {
        for (final Player p : server.connectedPlayers()) {
            if (!hasPermission(p, "bypass") && !settingsProxy.isWhitelisted(p.id())) {
                p.disconnect(translate(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    protected void kickPlayers(final Server server, final Server fallback) {
        final RegisteredServer fallbackServer = fallback != null ? ((VelocityServer) fallback).getServer() : null;
        final boolean checkForFallback = fallbackServer != null && !isMaintenance(fallback);
        for (final Player player : ((VelocityServer) server).getServer().connectedPlayers()) {
            if (!hasPermission(player, "bypass") && !settingsProxy.isWhitelisted(player.id())) {
                if (checkForFallback) {
                    player.sendMessage(translate(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName())));
                    // Kick the player if fallback server is not reachable
                    player.createConnectionRequest(fallbackServer).connect().whenComplete((result, e) -> {
                        if (!result.isSuccessful()) {
                            player.disconnect(translate(settingsProxy.getFullServerKickMessage(server.getName())));
                        }
                    });
                } else
                    player.disconnect(translate(settingsProxy.getFullServerKickMessage(server.getName())));
            } else {
                player.sendMessage(translate(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName())));
            }
        }
    }

    @Override
    protected void kickPlayersTo(final Server server) {
        final RegisteredServer waitingServer = ((VelocityServer) server).getServer();
        // Notifications done in global method
        for (final Player player : this.server.connectedPlayers()) {
            if (hasPermission(player, "bypass") || settingsProxy.isWhitelisted(player.id())) continue;
            if (player.connectedServer() != null && player.connectedServer().serverInfo().name().equals(waitingServer.serverInfo().name()))
                continue;
            if (!isMaintenance(waitingServer)) {
                player.createConnectionRequest(waitingServer).connect().whenComplete((result, e) -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(translate(settingsProxy.getMessage("sentToWaitingServer").replace("%SERVER%", server.getName())));
                    } else {
                        player.disconnect(translate(settingsProxy.getKickMessage()));
                    }
                });
            } else {
                player.disconnect(translate(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new VelocityTask(server.scheduler().buildTask(this, runnable).repeat(1, TimeUnit.SECONDS).schedule());
    }

    @Override
    @Nullable
    public Server getServer(final String server) {
        final RegisteredServer serverInfo = this.server.server(server);
        return serverInfo != null ? new VelocityServer(serverInfo) : null;
    }

    @Override
    @Nullable
    public SenderInfo getOfflinePlayer(final String name) {
        final Player player = server.player(name);
        if (player != null) {
            return new VelocitySenderInfo(player);
        }

        final ProfileLookup profile;
        try {
            profile = doUUIDLookup(name);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
        return new ProxyOfflineSenderInfo(profile.getUuid(), profile.getName());
    }

    @Override
    @Nullable
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final Player player = server.player(uuid);
        return player != null ? new VelocitySenderInfo(player) : null;
    }

    @Override
    @Nullable
    public String getServerNameOf(final SenderInfo sender) {
        final Player player = server.player(sender.getUuid());
        if (player == null || player.connectedServer() == null) return null;
        return player.connectedServer().serverInfo().name();
    }

    @Override
    public void async(final Runnable runnable) {
        server.scheduler().buildTask(this, runnable).schedule();
    }

    @Override
    protected void executeConsoleCommand(final String command) {
        server.commandManager().execute(server.consoleCommandSource(), command);
    }

    @Override
    public void broadcast(final String message) {
        server.sendMessage(translate(message));
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getPluginFile() {
        return server.pluginManager().getPlugin("maintenance").description().file().toFile();
    }

    @Override
    protected int getOnlinePlayers() {
        return server.countConnectedPlayers();
    }

    @Override
    protected int getMaxPlayers() {
        return server.configuration().getShowMaxPlayers();
    }

    @Override
    public InputStream getResource(final String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getServerVersion() {
        return server.version().version();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return server.pluginManager().plugins().stream().map(plugin ->
                new PluginDump(plugin.description().id() + "/" + plugin.description().name(),
                        plugin.description().version(), plugin.description().authors())).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws IOException {
        favicon = Favicon.create(file.toPath());
    }

    public boolean hasPermission(final CommandSource sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Favicon getFavicon() {
        return favicon;
    }

    public TextComponent translate(final String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
