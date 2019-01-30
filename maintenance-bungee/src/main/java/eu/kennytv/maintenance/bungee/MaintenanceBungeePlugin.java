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

import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.bungee.MaintenanceBungeeAPI;
import eu.kennytv.maintenance.api.proxy.IMaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommand;
import eu.kennytv.maintenance.bungee.command.MaintenanceBungeeCommandBase;
import eu.kennytv.maintenance.bungee.listener.PostLoginListener;
import eu.kennytv.maintenance.bungee.listener.ProxyPingListener;
import eu.kennytv.maintenance.bungee.listener.ServerConnectListener;
import eu.kennytv.maintenance.bungee.metrics.MetricsLite;
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.bungee.util.BungeeServer;
import eu.kennytv.maintenance.bungee.util.BungeeTask;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author KennyTV
 * @since 1.0
 */
public final class MaintenanceBungeePlugin extends MaintenanceProxyPlugin {
    private final MaintenanceBungeeBase plugin;
    private final SettingsProxy settings;
    private Favicon favicon;

    MaintenanceBungeePlugin(final MaintenanceBungeeBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.BUNGEE);
        this.plugin = plugin;
        sendEnableMessage();

        settings = new SettingsProxy(this);

        final PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(plugin, new PostLoginListener(this, settings));
        pm.registerListener(plugin, new ProxyPingListener(this, settings));
        pm.registerListener(plugin, new ServerConnectListener(this, settings));
        pm.registerCommand(plugin, new MaintenanceBungeeCommandBase(new MaintenanceBungeeCommand(this, settings)));

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (serverListPlus != null) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    @Deprecated
    public static IMaintenanceProxy getAPI() {
        return MaintenanceBungeeAPI.getAPI();
    }

    @Override
    protected void serverActions(final boolean maintenance) {
        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            getProxy().getPlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n")));
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    protected void serverActions(final Server server, final boolean maintenance) {
        final ServerInfo serverInfo = ((BungeeServer) server).getServer();
        if (maintenance) {
            final ServerInfo fallback = getProxy().getServerInfo(settings.getFallbackServer());
            if (fallback == null) {
                if (!serverInfo.getPlayers().isEmpty())
                    plugin.getLogger().warning("The fallback server set in the SpigotServers.yml could not be found! Instead kicking players from that server off the network!");
            } else if (fallback.equals(serverInfo))
                plugin.getLogger().warning("Maintenance has been enabled on the fallback server! If a player joins on a proxied server, they will be kicked completely instead of being sent to the fallback server!");
            serverInfo.getPlayers().forEach(p -> {
                if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                    if (fallback != null && fallback.canAccess(p)) {
                        p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", serverInfo.getName()));
                        p.connect(fallback);
                    } else
                        p.disconnect(settings.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", serverInfo.getName()));
                } else {
                    p.sendMessage(settings.getMessage("singleMaintenanceActivated").replace("%SERVER%", serverInfo.getName()));
                }
            });
        } else
            serverInfo.getPlayers().forEach(p -> p.sendMessage(settings.getMessage("singleMaintenanceDeactivated").replace("%SERVER%", server.getName())));
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        sender.sendMessage(getPrefix() + "§cThere is a newer version available: §aVersion " + getNewestVersion() + "§c, you're on §a" + getVersion());
        final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(getPrefix()));
        final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
        final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenancemode.40699/"));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
        tc1.addExtra(tc2);
        tc1.addExtra(click);
        ((BungeeSenderInfo) sender).sendMessage(tc1);
    }

    public boolean isMaintenance(final ServerInfo serverInfo) {
        return settings.getMaintenanceServers().contains(serverInfo.getName());
    }

    @Override
    protected Task startMaintenanceRunnable(final Runnable runnable) {
        return new BungeeTask(getProxy().getScheduler().schedule(plugin, runnable, 0, 1, TimeUnit.SECONDS).getId());
    }

    @Override
    public Server getServer(final String server) {
        final ServerInfo serverInfo = getProxy().getServerInfo(server);
        return serverInfo != null ? new BungeeServer(serverInfo) : null;
    }

    @Override
    public SenderInfo getPlayer(final String name) {
        final ProxiedPlayer player = getProxy().getPlayer(name);
        return player != null ? new BungeeSenderInfo(player) : null;
    }

    @Override
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null ? new BungeeSenderInfo(player) : null;
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
    public ISettings getSettings() {
        return settings;
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
    protected void loadIcon(final File file) throws IOException {
        favicon = Favicon.create(ImageIO.read(file));
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }

    public Favicon getFavicon() {
        return favicon;
    }
}