/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.Favicon;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
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
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author KennyTV
 * @since 3.0
 */
@Plugin(id = "maintenancevelocity", name = "MaintenanceVelocity", version = MaintenanceVersion.VERSION, authors = "KennyTV",
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
        server.getCommandManager().register(command, "maintenance", "maintenancevelocity");
        final EventManager em = server.getEventManager();
        em.register(this, ProxyPingEvent.class, PostOrder.LAST, new ProxyPingListener(this, settingsProxy));
        em.register(this, new ServerConnectListener(this, settingsProxy));

        continueLastEndtimer();

        // ServerListPlus integration
        server.getPluginManager().getPlugin("serverlistplus").ifPresent(slpContainer -> slpContainer.getInstance().ifPresent(serverListPlus -> {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settingsProxy.isMaintenance());
            logger.info("Enabled ServerListPlus integration!");
        }));
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
        final TextComponent tc1 = translate(getPrefix());
        final TextComponent tc2 = translate("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/");
        final TextComponent click = translate(" §7§l§o(CLICK ME)");
        click.clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/maintenance.40699/"));
        click.hoverEvent(HoverEvent.showText(TextComponent.of("§aDownload the latest version")));
        tc1.append(tc2);
        tc1.append(click);
        ((VelocitySenderInfo) sender).sendMessage(tc1);
    }

    public boolean isMaintenance(final RegisteredServer serverInfo) {
        return settingsProxy.isMaintenance(serverInfo.getServerInfo().getName());
    }

    @Override
    protected void kickPlayersFromProxy() {
        for (final Player p : server.getAllPlayers()) {
            if (!hasPermission(p, "bypass") && !settingsProxy.isWhitelisted(p.getUniqueId())) {
                p.disconnect(TextComponent.of(settingsProxy.getKickMessage()));
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
                    player.sendMessage(translate(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName())));
                    // Kick the player if fallback server is not reachable
                    player.createConnectionRequest(fallbackServer).connect().whenComplete((result, e) -> {
                        if (!result.isSuccessful()) {
                            player.disconnect(TextComponent.of(settingsProxy.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", server.getName())));
                        }
                    });
                } else
                    player.disconnect(TextComponent.of(settingsProxy.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", server.getName())));
            } else {
                player.sendMessage(translate(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName())));
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
                        player.sendMessage(translate(settingsProxy.getMessage("sentToWaitingServer").replace("%SERVER%", server.getName())));
                    } else {
                        player.disconnect(TextComponent.of(settingsProxy.getKickMessage()));
                    }
                });
            } else {
                player.disconnect(TextComponent.of(settingsProxy.getKickMessage()));
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
    @Nullable
    public SenderInfo getOfflinePlayer(final String name) {
        final Optional<Player> player = server.getPlayer(name);
        return player.map(VelocitySenderInfo::new).orElse(null);
    }

    @Override
    @Nullable
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final Optional<Player> player = server.getPlayer(uuid);
        return player.map(VelocitySenderInfo::new).orElse(null);
    }

    @Override
    @Nullable
    public String getServer(final SenderInfo sender) {
        final Optional<Player> player = server.getPlayer(sender.getUuid());
        if (!player.isPresent() || !player.get().getCurrentServer().isPresent()) return null;
        return player.get().getCurrentServer().get().getServerInfo().getName();
    }

    @Override
    public void async(final Runnable runnable) {
        server.getScheduler().buildTask(this, runnable).schedule();
    }

    @Override
    public void broadcast(final String message) {
        server.broadcast(translate(message));
        logger.info(message);
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getPluginFile() {
        return server.getPluginManager().getPlugin("maintenancevelocity")
                .orElseThrow(() -> new IllegalArgumentException("Couldn't get Maintenance instance. Custom/broken build?")).getDescription().getSource()
                .orElseThrow(IllegalArgumentException::new).toFile();
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

    public ProxyServer getServer() {
        return server;
    }

    public Favicon getFavicon() {
        return favicon;
    }

    public TextComponent translate(final String s) {
        return LegacyComponentSerializer.INSTANCE.deserialize(s);
    }
}
