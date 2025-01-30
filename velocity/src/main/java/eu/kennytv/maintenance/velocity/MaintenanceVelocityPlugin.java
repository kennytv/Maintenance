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
package eu.kennytv.maintenance.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.Favicon;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.hook.LuckPermsProxyHook;
import eu.kennytv.maintenance.core.proxy.util.ProfileLookup;
import eu.kennytv.maintenance.core.proxy.util.ProxyOfflineSenderInfo;
import eu.kennytv.maintenance.core.util.MaintenanceVersion;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.velocity.command.MaintenanceVelocityCommand;
import eu.kennytv.maintenance.velocity.listener.ProxyPingListener;
import eu.kennytv.maintenance.velocity.listener.ServerConnectListener;
import eu.kennytv.maintenance.velocity.util.ComponentUtil;
import eu.kennytv.maintenance.velocity.util.LoggerWrapper;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import eu.kennytv.maintenance.velocity.util.VelocityServer;
import eu.kennytv.maintenance.velocity.util.VelocityTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;

@Plugin(id = "maintenance", name = "Maintenance", version = MaintenanceVersion.VERSION, authors = "kennytv",
        description = "Enable maintenance mode with a custom maintenance motd and icon.", url = MaintenancePlugin.HANGAR_URL,
        dependencies = {@Dependency(id = "serverlistplus", optional = true), @Dependency(id = "luckperms", optional = true)})
public final class MaintenanceVelocityPlugin extends MaintenanceProxyPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final File dataFolder;
    private final Metrics.Factory metricsFactory;
    private Favicon favicon;

    @Inject
    public MaintenanceVelocityPlugin(final ProxyServer server, final org.slf4j.Logger logger, @DataDirectory final Path folder, final Metrics.Factory metricsFactory) {
        super(MaintenanceVersion.VERSION, ServerType.VELOCITY);
        this.server = server;
        this.logger = new LoggerWrapper(logger);
        this.dataFolder = folder.toFile();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onEnable(final ProxyInitializeEvent event) {
        settingsProxy = new SettingsProxy(this);
        settings = settingsProxy;

        metricsFactory.make(this, 16502);

        sendEnableMessage();

        final MaintenanceVelocityCommand command = new MaintenanceVelocityCommand(this, settingsProxy);
        commandManager = command;
        server.getCommandManager().register(server.getCommandManager().metaBuilder("maintenance").aliases("mt").build(), command);

        final EventManager em = server.getEventManager();
        em.register(this, ProxyPingEvent.class, PostOrder.LAST, new ProxyPingListener(this, settingsProxy));
        em.register(this, new ServerConnectListener(this, settingsProxy));

        continueLastEndtimer();

        final PluginManager pluginManager = server.getPluginManager();
        pluginManager.getPlugin("serverlistplus").flatMap(PluginContainer::getInstance).ifPresent(serverListPlus -> {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            if (settings.isEnablePingMessages()) {
                serverListPlusHook.setEnabled(!settingsProxy.isMaintenance());
            }
            logger.info("Enabled ServerListPlus integration!");
        });

        if (false && pluginManager.getPlugin("luckperms").isPresent()) {
            LuckPermsProxyHook.<Player>register(this, player -> player.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse(null));
            logger.info("Registered LuckPerms context");
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

    public boolean isMaintenance(final RegisteredServer serverInfo) {
        return settingsProxy.isMaintenance(serverInfo.getServerInfo().getName());
    }

    @Override
    protected void kickPlayersFromProxy() {
        for (final Player p : server.getAllPlayers()) {
            if (!hasPermission(p, "bypass") && !settingsProxy.isWhitelisted(p.getUniqueId())) {
                p.disconnect(ComponentUtil.toVelocity(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    protected void kickPlayers(final Server server, final Server fallback) {
        final RegisteredServer fallbackServer = fallback != null ? ((VelocityServer) fallback).getServer() : null;
        final boolean checkForFallback = fallbackServer != null && !isMaintenance(fallback);
        for (final Player player : ((VelocityServer) server).getServer().getPlayersConnected()) {
            if (!hasPermission(player, "bypass") && !settingsProxy.isWhitelisted(player.getUniqueId())) {
                if (checkForFallback) {
                    player.sendMessage(ComponentUtil.toVelocity(settingsProxy.getMessage("singleMaintenanceActivated", "%SERVER%", server.getName())));
                    // Kick the player if fallback server is not reachable
                    player.createConnectionRequest(fallbackServer).connect().whenComplete((result, e) -> {
                        if (!result.isSuccessful()) {
                            player.disconnect(ComponentUtil.toVelocity(settingsProxy.getFullServerKickMessage(server.getName())));
                        }
                    });
                } else
                    player.disconnect(ComponentUtil.toVelocity(settingsProxy.getFullServerKickMessage(server.getName())));
            } else {
                player.sendMessage(ComponentUtil.toVelocity(settingsProxy.getMessage("singleMaintenanceActivated", "%SERVER%", server.getName())));
            }
        }
    }

    @Override
    protected void kickPlayersTo(final Server server) {
        final RegisteredServer waitingServer = ((VelocityServer) server).getServer();
        // Notifications done in global method
        for (final Player player : this.server.getAllPlayers()) {
            if (hasPermission(player, "bypass") || settingsProxy.isWhitelisted(player.getUniqueId())) continue;
            if (player.getCurrentServer().isPresent() && player.getCurrentServer().get().getServerInfo().getName().equals(waitingServer.getServerInfo().getName()))
                continue;
            if (!isMaintenance(waitingServer)) {
                player.createConnectionRequest(waitingServer).connect().whenComplete((result, e) -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(ComponentUtil.toVelocity(settingsProxy.getMessage("sentToWaitingServer", "%SERVER%", server.getName())));
                    } else {
                        player.disconnect(ComponentUtil.toVelocity(settingsProxy.getKickMessage()));
                    }
                });
            } else {
                player.disconnect(ComponentUtil.toVelocity(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new VelocityTask(server.getScheduler().buildTask(this, runnable).repeat(1, TimeUnit.SECONDS).schedule());
    }

    @Override
    @Nullable
    public Server getServer(final String server) {
        final Optional<RegisteredServer> serverInfo = this.server.getServer(server);
        return serverInfo.map(VelocityServer::new).orElse(null);
    }

    @Override
    public Set<String> getServers() {
        return server.getAllServers().stream().map(server -> server.getServerInfo().getName()).collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(final String name) {
        final Optional<Player> player = server.getPlayer(name);
        if (player.isPresent()) {
            return CompletableFuture.completedFuture(new VelocitySenderInfo(player.get()));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                final ProfileLookup profile = doUUIDLookup(name);
                return new ProxyOfflineSenderInfo(profile.getUuid(), profile.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(final UUID uuid) {
        final Optional<Player> player = server.getPlayer(uuid);
        return CompletableFuture.completedFuture(player.map(VelocitySenderInfo::new).orElse(null));
    }

    @Override
    @Nullable
    public String getServerNameOf(final SenderInfo sender) {
        final Optional<Player> player = server.getPlayer(sender.getUuid());
        if (!player.isPresent() || !player.get().getCurrentServer().isPresent()) return null;
        return player.get().getCurrentServer().get().getServerInfo().getName();
    }

    @Override
    public void async(final Runnable runnable) {
        server.getScheduler().buildTask(this, runnable).schedule();
    }

    @Override
    protected void executeConsoleCommand(final String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public void broadcast(final Component component) {
        server.sendMessage(ComponentUtil.toVelocity(component));
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getPluginFile() {
        return server.getPluginManager().getPlugin("maintenance")
                .orElseThrow(() -> new IllegalArgumentException("Couldn't get Maintenance instance. Custom/broken build?")).getDescription().getSource()
                .orElseThrow(IllegalArgumentException::new).toFile();
    }

    @Override
    protected int getOnlinePlayers() {
        return server.getPlayerCount();
    }

    @Override
    protected int getMaxPlayers() {
        return server.getConfiguration().getShowMaxPlayers();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getServerVersion() {
        return server.getVersion().getVersion();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return server.getPluginManager().getPlugins().stream().map(plugin ->
                new PluginDump(plugin.getDescription().getId() + "/" + plugin.getDescription().getName().orElse("-"),
                        plugin.getDescription().getVersion().orElse("-"), plugin.getDescription().getAuthors())).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws IOException {
        favicon = Favicon.create(ImageIO.read(file));
    }

    public boolean hasPermission(final CommandSource sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    @Override
    public void removeWhitelist(UUID uuid) {
        super.removeWhitelist(uuid);
    }

    @Override
    public void addWhitelist(UUID uuid, String player) {
        super.addWhitelist(uuid, player);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Favicon getFavicon() {
        return favicon;
    }
}
