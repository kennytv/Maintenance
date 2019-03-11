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

package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.spigot.MaintenanceSpigotAPI;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.spigot.command.MaintenanceSpigotCommand;
import eu.kennytv.maintenance.spigot.listener.PaperServerListPingListener;
import eu.kennytv.maintenance.spigot.listener.PlayerLoginListener;
import eu.kennytv.maintenance.spigot.listener.ServerInfoPacketListener;
import eu.kennytv.maintenance.spigot.listener.ServerListPingListener;
import eu.kennytv.maintenance.spigot.metrics.MetricsLite;
import eu.kennytv.maintenance.spigot.util.BukkitOfflinePlayerInfo;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import eu.kennytv.maintenance.spigot.util.BukkitTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author KennyTV
 * @since 2.0
 */
public final class MaintenanceSpigotPlugin extends MaintenancePlugin {
    private final MaintenanceSpigotBase plugin;
    private CachedServerIcon favicon;

    MaintenanceSpigotPlugin(final MaintenanceSpigotBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.SPIGOT);
        this.plugin = plugin;

        settings = new Settings(this, "mysql", "proxied-maintenance-servers", "fallback");

        sendEnableMessage();

        final MaintenanceSpigotCommand command = new MaintenanceSpigotCommand(this, settings);
        commandManager = command;
        plugin.getCommand("maintenancespigot").setExecutor(command);

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerLoginListener(this, settings), plugin);

        if (isAcceptablePaper()) {
            pm.registerEvents(new PaperServerListPingListener(this, settings), plugin);
        } else {
            if (pm.isPluginEnabled("ProtocolLib")) {
                pm.registerEvents(new ServerInfoPacketListener(this, plugin, settings), plugin);
            } else {
                pm.registerEvents(new ServerListPingListener(this, settings), plugin);
                getLogger().warning("To use this plugin on Spigot to its full extend, you need the plugin ProtocolLib!");
            }
        }

        new MetricsLite(plugin);

        // ServerListPlus integration
        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (pm.isPluginEnabled(serverListPlus)) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            plugin.getLogger().info("Enabled ServerListPlus integration!");
        }
    }

    private boolean isAcceptablePaper() {
        try {
            Class.forName("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpigotAPI.getAPI();
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new BukkitTask(getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 20));
    }

    @Override
    public void async(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void broadcast(final String message) {
        getServer().broadcastMessage(message);
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
        ((BukkitSenderInfo) sender).sendMessage(tc1, getPrefix() + "§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/");
    }

    @Override
    public SenderInfo getOfflinePlayer(final String name) {
        final OfflinePlayer player = getServer().getOfflinePlayer(name);
        return player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null;
    }

    @Override
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final OfflinePlayer player = getServer().getOfflinePlayer(uuid);
        return player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null;
    }

    @Override
    protected void kickPlayers() {
        getServer().getOnlinePlayers().stream()
                .filter(p -> !hasPermission(p, "bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                .forEach(p -> p.kickPlayer(settings.getKickMessage()));
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
        return plugin.getResource(name);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public String getServerVersion() {
        return getServer().getVersion();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return Arrays.stream(getServer().getPluginManager().getPlugins()).map(plugin ->
                new PluginDump(plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthors())).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws Exception {
        favicon = plugin.getServer().loadServerIcon(ImageIO.read(file));
    }

    public boolean hasPermission(final CommandSender sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public CachedServerIcon getFavicon() {
        return favicon;
    }
}
