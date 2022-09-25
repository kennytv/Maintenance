/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
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

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommand;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommandBase;
import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.listener.ServerConnectListener;
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.bungee.util.BungeeServer;
import eu.kennytv.maintenance.bungee.util.BungeeTask;
import eu.kennytv.maintenance.bungee.util.ComponentUtil;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.hook.LuckPermsProxyHook;
import eu.kennytv.maintenance.core.proxy.util.ProfileLookup;
import eu.kennytv.maintenance.core.proxy.util.ProxyOfflineSenderInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.lib.kyori.adventure.platform.bungeecord.BungeeAudiences;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MaintenanceBungeePlugin extends MaintenanceProxyPlugin {
    private final MaintenanceBungeeBase plugin;
    private final BungeeAudiences audiences;
    private Favicon favicon;

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.BUNGEE);
        this.plugin = plugin;
        this.audiences = BungeeAudiences.create(plugin);

        settingsProxy = new SettingsProxy(this);
        settings = settingsProxy;

        sendEnableMessage();

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new ProxyPingListener(this, settingsProxy));
        pm.registerListener(plugin, new ServerConnectListener(this, settingsProxy));
        commandManager = new MaintenanceBungeeCommand(this, settingsProxy);
        pm.registerCommand(plugin, new MaintenanceBungeeCommandBase(commandManager));

        continueLastEndtimer();
        new Metrics(plugin, 742);

        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (serverListPlus != null) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            if (settings.isEnablePingMessages()) {
                serverListPlusHook.setEnabled(!settingsProxy.isMaintenance());
            }
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }

        if (false && pm.getPlugin("LuckPerms") != null) {
            LuckPermsProxyHook.<ProxiedPlayer>register(this, player -> {
                final net.md_5.bungee.api.connection.Server server = player.getServer();
                return server != null ? server.getInfo().getName() : null;
            });
            getLogger().info("Registered LuckPerms context");
        }
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(getPrefix()));
        final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/"));
        final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenance.40699/"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("§aDownload the latest version").create())));
        tc1.addExtra(tc2);
        tc1.addExtra(click);
        ((BungeeSenderInfo) sender).sendMessage(tc1);
    }

    public boolean isMaintenance(final ServerInfo serverInfo) {
        return settingsProxy.isMaintenance(serverInfo.getName());
    }

    @Override
    protected void kickPlayersFromProxy() {
        for (final ProxiedPlayer player : getProxy().getPlayers()) {
            if (!hasPermission(player, "bypass") && !settingsProxy.isWhitelisted(player.getUniqueId())) {
                player.disconnect(ComponentUtil.toBadComponents(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    protected void kickPlayers(final Server server, final Server fallback) {
        // Kick players from a proxied server
        final ServerInfo fallbackServer = fallback != null ? ((BungeeServer) fallback).getServer() : null;
        final boolean checkForFallback = fallbackServer != null && !isMaintenance(fallback);
        for (final ProxiedPlayer player : ((BungeeServer) server).getServer().getPlayers()) {
            if (!hasPermission(player, "bypass") && !settingsProxy.isWhitelisted(player.getUniqueId())) {
                if (checkForFallback && fallbackServer.canAccess(player)) {
                    audiences.player(player).sendMessage(settingsProxy.getMessage("singleMaintenanceActivated", "%SERVER%", server.getName()));
                    player.connect(fallbackServer);
                } else {
                    player.disconnect(ComponentUtil.toBadComponents(settingsProxy.getFullServerKickMessage(server.getName())));
                }
            } else {
                audiences.player(player).sendMessage(settingsProxy.getMessage("singleMaintenanceActivated", "%SERVER%", server.getName()));
            }
        }
    }

    @Override
    protected void kickPlayersTo(final Server server) {
        // Kick all players to a single waiting server
        final ServerInfo serverInfo = ((BungeeServer) server).getServer();
        // Notifications done in global method
        for (final ProxiedPlayer player : getProxy().getPlayers()) {
            if (hasPermission(player, "bypass") || settingsProxy.isWhitelisted(player.getUniqueId())) continue;
            if (player.getServer() != null && player.getServer().getInfo().getName().equals(serverInfo.getName()))
                continue;
            if (serverInfo.canAccess(player) && !isMaintenance(serverInfo)) {
                audiences.player(player).sendMessage(settingsProxy.getMessage("sentToWaitingServer", "%SERVER%", server.getName()));
                player.connect(serverInfo);
            } else {
                player.disconnect(ComponentUtil.toBadComponents(settingsProxy.getKickMessage()));
            }
        }
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new BungeeTask(getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId());
    }

    @Override
    public void getOfflinePlayer(final String name, final Consumer<@Nullable SenderInfo> consumer) {
        final ProxiedPlayer player = getProxy().getPlayer(name);
        if (player != null) {
            consumer.accept(new BungeeSenderInfo(player));
            return;
        }

        getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                final ProfileLookup profile = doUUIDLookup(name);
                consumer.accept(new ProxyOfflineSenderInfo(profile.getUuid(), profile.getName()));
            } catch (final IOException e) {
                e.printStackTrace();
                consumer.accept(null);
            }
        });
    }

    @Override
    public void getOfflinePlayer(final UUID uuid, final Consumer<@Nullable SenderInfo> consumer) {
        final ProxiedPlayer player = getProxy().getPlayer(uuid);
        consumer.accept(player != null ? new BungeeSenderInfo(player) : null);
    }

    @Override
    @Nullable
    public Server getServer(final String server) {
        final ServerInfo serverInfo = getProxy().getServerInfo(server);
        return serverInfo != null ? new BungeeServer(serverInfo) : null;
    }

    @Override
    public Set<String> getServers() {
        return getProxy().getServers().keySet();
    }

    @Override
    @Nullable
    public String getServerNameOf(final SenderInfo sender) {
        final ProxiedPlayer player = getProxy().getPlayer(sender.getUuid());
        if (player == null || player.getServer() == null) return null;
        return player.getServer().getInfo().getName();
    }

    @Override
    public void async(final Runnable runnable) {
        getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    protected void executeConsoleCommand(final String command) {
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
    }

    @Override
    public void broadcast(final Component component) {
        audiences.all().sendMessage(component);
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
    protected int getOnlinePlayers() {
        return getProxy().getOnlineCount();
    }

    @Override
    protected int getMaxPlayers() {
        return getProxy().getConfig().getPlayerLimit();
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

    public BungeeAudiences audiences() {
        return audiences;
    }
}