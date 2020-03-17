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

import eu.kennytv.maintenance.api.bungee.MaintenanceBungeeAPI;
import eu.kennytv.maintenance.api.proxy.IMaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommand;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommandBase;
import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.listener.ServerConnectListener;
import eu.kennytv.maintenance.bungee.metrics.MetricsLite;
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.bungee.util.BungeeServer;
import eu.kennytv.maintenance.bungee.util.BungeeTask;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeePlugin extends MaintenanceProxyPlugin {
    private final MaintenanceBungeeBase plugin;
    private Favicon favicon;

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.BUNGEE);
        this.plugin = plugin;

        settingsProxy = new SettingsProxy(this);
        settings = settingsProxy;

        sendEnableMessage();

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new ProxyPingListener(this, settingsProxy));
        pm.registerListener(plugin, new ServerConnectListener(this, settingsProxy));
        commandManager = new MaintenanceBungeeCommand(this, settingsProxy);
        pm.registerCommand(plugin, new MaintenanceBungeeCommandBase(commandManager));

        continueLastEndtimer();
        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (serverListPlus != null) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settingsProxy.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Deprecated
    public static IMaintenanceProxy getAPI() {
        return MaintenanceBungeeAPI.getAPI();
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(getPrefix()));
        final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/"));
        final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenance.40699/"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
        tc1.addExtra(tc2);
        tc1.addExtra(click);
        ((BungeeSenderInfo) sender).sendMessage(tc1);
    }

    public boolean isMaintenance(final ServerInfo serverInfo) {
        return settingsProxy.isMaintenance(serverInfo.getName());
    }

    @Override
    protected void kickPlayersFromProxy() {
        for (final ProxiedPlayer p : getProxy().getPlayers()) {
            if (!hasPermission(p, "bypass") && !settingsProxy.isWhitelisted(p.getUniqueId())) {
                p.disconnect(settingsProxy.getKickMessage());
            }
        }
    }

    @Override
    protected void kickPlayers(final Server server, final Server fallback) {
        final ServerInfo fallbackServer = fallback != null ? ((BungeeServer) fallback).getServer() : null;
        final boolean checkForFallback = fallbackServer != null && !isMaintenance(fallback);
        for (final ProxiedPlayer player : ((BungeeServer) server).getServer().getPlayers()) {
            if (!hasPermission(player, "bypass") && !settingsProxy.isWhitelisted(player.getUniqueId())) {
                if (checkForFallback && fallbackServer.canAccess(player)) {
                    player.sendMessage(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
                    player.connect(fallbackServer);
                } else
                    player.disconnect(settingsProxy.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", server.getName()));
            } else {
                player.sendMessage(settingsProxy.getMessage("singleMaintenanceActivated").replace("%SERVER%", server.getName()));
            }
        }
    }

    @Override
    protected void kickPlayersTo(final Server server) {
        final ServerInfo serverInfo = ((BungeeServer) server).getServer();
        // Notifications done in global method
        for (final ProxiedPlayer player : getProxy().getPlayers()) {
            if (hasPermission(player, "bypass") || settingsProxy.isWhitelisted(player.getUniqueId())) continue;
            if (player.getServer() != null && player.getServer().getInfo().getName().equals(serverInfo.getName())) continue;
            if (serverInfo.canAccess(player) && !isMaintenance(serverInfo)) {
                player.sendMessage(settingsProxy.getMessage("sentToWaitingServer").replace("%SERVER%", server.getName()));
                player.connect(serverInfo);
            } else {
                player.disconnect(settingsProxy.getKickMessage());
            }
        }
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new BungeeTask(getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId());
    }

    @Override
    public Server getServer(final String server) {
        final ServerInfo serverInfo = getProxy().getServerInfo(server);
        return serverInfo != null ? new BungeeServer(serverInfo) : null;
    }

    @Override
    public SenderInfo getOfflinePlayer(final String name) {
        final ProxiedPlayer player = getProxy().getPlayer(name);
        return player != null ? new BungeeSenderInfo(player) : null;
    }

    @Override
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null ? new BungeeSenderInfo(player) : null;
    }

    @Override
    public String getServer(final SenderInfo sender) {
        final ProxiedPlayer player = getProxy().getPlayer(sender.getUuid());
        if (player == null || player.getServer() == null) return null;
        return player.getServer().getInfo().getName();
    }

    @Override
    public void async(final Runnable runnable) {
        getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void broadcast(final String message) {
        getProxy().broadcast(message);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public File getPluginFile() {
        return plugin.getPluginFile();
    }

    @Override
    public InputStream getResource(final String name) {
        return plugin.getResourceAsStream(name);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public String getServerVersion() {
        return getProxy().getVersion();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return getProxy().getPluginManager().getPlugins().stream().map(plugin ->
                new PluginDump(plugin.getDescription().getName(), plugin.getDescription().getVersion(), Arrays.asList(plugin.getDescription().getAuthor()))).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws IOException {
        favicon = Favicon.create(ImageIO.read(file));
    }

    public boolean hasPermission(final CommandSender sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }

    public Favicon getFavicon() {
        return favicon;
    }
}