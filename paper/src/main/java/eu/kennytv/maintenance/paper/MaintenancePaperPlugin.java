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
package eu.kennytv.maintenance.paper;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.LuckPermsHook;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.lib.kyori.adventure.platform.bukkit.BukkitAudiences;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.paper.command.MaintenancePaperCommand;
import eu.kennytv.maintenance.paper.listener.PaperServerListPingListener;
import eu.kennytv.maintenance.paper.listener.PlayerLoginListener;
import eu.kennytv.maintenance.paper.listener.ServerInfoPacketListener;
import eu.kennytv.maintenance.paper.listener.ServerListPingListener;
import eu.kennytv.maintenance.paper.util.BukkitOfflinePlayerInfo;
import eu.kennytv.maintenance.paper.util.BukkitTask;
import eu.kennytv.maintenance.paper.util.ComponentUtil;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.bstats.bukkit.Metrics;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

public final class MaintenancePaperPlugin extends MaintenancePlugin {
    private static final boolean FOLIA = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    private final MaintenancePaperBase plugin;
    private final BukkitAudiences audiences;
    private CachedServerIcon favicon;

    MaintenancePaperPlugin(final MaintenancePaperBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.SPIGOT);
        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);

        settings = new Settings(this, "mysql", "proxied-maintenance-servers", "fallback", "waiting-server", "commands-on-single-maintenance-enable", "commands-on-single-maintenance-disable");

        sendEnableMessage();

        final MaintenancePaperCommand command = new MaintenancePaperCommand(this, settings);
        commandManager = command;
        plugin.getCommand("maintenance").setExecutor(command);

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerLoginListener(this, settings), plugin);

        if (canUsePaperListener()) {
            pm.registerEvents(new PaperServerListPingListener(this, settings), plugin);
        } else if (pm.isPluginEnabled("ProtocolLib")) {
            pm.registerEvents(new ServerInfoPacketListener(this, plugin, settings), plugin);
        } else {
            pm.registerEvents(new ServerListPingListener(this, settings), plugin);
            getLogger().warning("To use this plugin on Spigot to its full extent, you need the plugin ProtocolLib!");
        }

        continueLastEndtimer();
        new Metrics(plugin, 2205);

        final Plugin serverListPlus = pm.getPlugin("ServerListPlus");
        if (pm.isPluginEnabled(serverListPlus)) {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            if (settings.isEnablePingMessages()) {
                serverListPlusHook.setEnabled(!settings.isMaintenance());
            }
            plugin.getLogger().info("Enabled ServerListPlus integration");
        }

        if (false && pm.isPluginEnabled("LuckPerms")) {
            LuckPermsHook.<Player>register(this);
            plugin.getLogger().info("Registered LuckPerms context");
        }
    }

    private static boolean hasClass(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    private boolean canUsePaperListener() {
        try {
            Class.forName("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
            if (getServer().getPluginManager().isPluginEnabled("ProtocolSupport")) {
                getLogger().warning("Found ProtocolSupport - switching to ProtocolLib packet adapter, as PS does not fire Paper's ping event");
                return false;
            }

            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        if (FOLIA) {
            throw new UnsupportedOperationException("Scheduling tasks is not yet supported on Folia");
        }
        return new BukkitTask(getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 20));
    }

    @Override
    public CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(final String name) {
        final OfflinePlayer player = getServer().getOfflinePlayer(name);
        return CompletableFuture.completedFuture(player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null);
    }

    @Override
    public CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(final UUID uuid) {
        final OfflinePlayer player = getServer().getOfflinePlayer(uuid);
        return CompletableFuture.completedFuture(player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null);
    }

    @Override
    public void async(final Runnable runnable) {
        if (FOLIA) {
            // Preliminary Folia support
            runnable.run();
            return;
        }
        getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    protected void executeConsoleCommand(final String command) {
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }

    @Override
    public void broadcast(final Component component) {
        audiences.all().sendMessage(component);
    }

    @Override
    protected void kickPlayers() {
        for (final Player p : getServer().getOnlinePlayers()) {
            if (!hasPermission(p, "bypass") && !settings.isWhitelisted(p.getUniqueId())) {
                final Component kickMessage = settings.getKickMessage();
                if (ComponentUtil.PAPER) {
                    p.kick(ComponentUtil.toPaperComponent(kickMessage));
                } else {
                    p.kickPlayer(ComponentUtil.toLegacy(kickMessage));
                }
            }
        }
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
        return getServer().getOnlinePlayers().size();
    }

    @Override
    protected int getMaxPlayers() {
        return getServer().getMaxPlayers();
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

    public BukkitAudiences audiences() {
        return audiences;
    }
}
