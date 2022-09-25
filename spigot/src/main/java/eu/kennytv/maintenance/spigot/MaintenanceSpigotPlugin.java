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
package eu.kennytv.maintenance.spigot;

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
import eu.kennytv.maintenance.spigot.command.MaintenanceSpigotCommand;
import eu.kennytv.maintenance.spigot.listener.PaperServerListPingListener;
import eu.kennytv.maintenance.spigot.listener.PlayerLoginListener;
import eu.kennytv.maintenance.spigot.listener.ServerInfoPacketListener;
import eu.kennytv.maintenance.spigot.listener.ServerListPingListener;
import eu.kennytv.maintenance.spigot.util.BukkitOfflinePlayerInfo;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import eu.kennytv.maintenance.spigot.util.BukkitTask;
import eu.kennytv.maintenance.spigot.util.ComponentUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MaintenanceSpigotPlugin extends MaintenancePlugin {
    private final MaintenanceSpigotBase plugin;
    private final BukkitAudiences audiences;
    private CachedServerIcon favicon;

    MaintenanceSpigotPlugin(final MaintenanceSpigotBase plugin) {
        super(plugin.getDescription().getVersion(), ServerType.SPIGOT);
        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);

        settings = new Settings(this, "mysql", "proxied-maintenance-servers", "fallback", "waiting-server");

        sendEnableMessage();

        final MaintenanceSpigotCommand command = new MaintenanceSpigotCommand(this, settings);
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
            getLogger().warning("To use this plugin on Spigot to its full extend, you need the plugin ProtocolLib!");
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
        return new BukkitTask(getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 20));
    }

    @Override
    public void getOfflinePlayer(final String name, final Consumer<@Nullable SenderInfo> consumer) {
        final OfflinePlayer player = getServer().getOfflinePlayer(name);
        consumer.accept(player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null);
    }

    @Override
    public void getOfflinePlayer(final UUID uuid, final Consumer<@Nullable SenderInfo> consumer) {
        final OfflinePlayer player = getServer().getOfflinePlayer(uuid);
        consumer.accept(player.getName() != null ? new BukkitOfflinePlayerInfo(player) : null);
    }

    @Override
    public void async(final Runnable runnable) {
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
