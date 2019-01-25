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

package eu.kennytv.maintenance.sponge;

import com.google.inject.Inject;
import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.api.sponge.MaintenanceSpongeAPI;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.MaintenanceVersion;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.sponge.command.MaintenanceSpongeCommand;
import eu.kennytv.maintenance.sponge.listener.ClientConnectionListener;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import eu.kennytv.maintenance.sponge.listener.GameReloadListener;
import eu.kennytv.maintenance.sponge.util.LoggerWrapper;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import eu.kennytv.maintenance.sponge.util.SpongeTask;
import org.bstats.sponge.Metrics2;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author KennyTV
 * @since 3.0
 */
@Plugin(id = "maintenancesponge", name = "MaintenanceSponge", version = MaintenanceVersion.VERSION, authors = "KennyTV",
        description = "Enable maintenance mode with a custom maintenance motd and icon.", url = "https://www.spigotmc.org/resources/maintenancemode.40699/",
        dependencies = @Dependency(id = "serverlistplus", optional = true))
public final class MaintenanceSpongePlugin extends MaintenanceModePlugin {
    private SettingsSponge settings;
    private Logger logger;
    @Inject private Game game;
    @Inject private PluginContainer container;
    @Inject private Metrics2 metrics;
    @Inject @ConfigDir(sharedRoot = false)
    private File dataFolder;

    @Inject
    public MaintenanceSpongePlugin() {
        super(MaintenanceVersion.VERSION, ServerType.SPONGE);
    }

    @Listener
    public void onEnable(final GameInitializationEvent event) {
        logger = new LoggerWrapper(container.getLogger());
        sendEnableMessage();

        settings = new SettingsSponge(this);

        game.getCommandManager().register(this, new MaintenanceSpongeCommand(this, settings), "maintenance", "maintenancesponge");
        final EventManager em = game.getEventManager();
        em.registerListeners(this, new ClientConnectionListener(this, settings));
        em.registerListeners(this, new ClientPingServerListener(this, settings));
        em.registerListeners(this, new GameReloadListener(this));

        // ServerListPlus integration
        game.getPluginManager().getPlugin("serverlistplus").ifPresent(slpContainer -> slpContainer.getInstance().ifPresent(serverListPlus -> {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            logger.info("Enabled ServerListPlus integration!");
        }));
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpongeAPI.getAPI();
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.getConfig().set("enable-maintenance-mode", maintenance);
        settings.saveConfig();
        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);
        if (isTaskRunning())
            cancelTask();
        if (maintenance) {
            getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                    .forEach(p -> p.kick(Text.of(settings.getKickMessage().replace("%NEWLINE%", "\n"))));
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        return new SpongeTask(game.getScheduler().createTaskBuilder().execute(runnable).interval(1, TimeUnit.SECONDS).submit(this));
    }

    @Override
    public void async(final Runnable runnable) {
        game.getScheduler().createTaskBuilder().async().execute(runnable).submit(this);
    }

    @Override
    public void broadcast(final String message) {
        getServer().getBroadcastChannel().send(Text.of(message));
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        sender.sendMessage(getPrefix() + "§cThere is a newer version available: §aVersion " + getNewestVersion() + "§c, you're on §a" + getVersion());
        Text text;
        try {
            text = Text.builder(getPrefix())
                    .append(Text.of("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/ "))
                    .append(Text.builder("§7§l§o(CLICK ME)")
                            .onClick(TextActions.openUrl(new URL("https://www.spigotmc.org/resources/maintenancemode.40699/")))
                            .onHover(TextActions.showText(Text.of("§aDownload the latest version"))).build()).build();
        } catch (final MalformedURLException e) {
            text = Text.of("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/");
            e.printStackTrace();
        }
        ((SpongeSenderInfo) sender).sendMessage(text);
    }

    @Override
    public SenderInfo getPlayer(final String name) {
        final Optional<Player> player = getServer().getPlayer(name);
        return player.map(SpongeSenderInfo::new).orElse(null);
    }

    @Override
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final Optional<Player> player = getServer().getPlayer(uuid);
        return player.map(SpongeSenderInfo::new).orElse(null);
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public File getPluginFile() {
        final Optional<Path> source = container.getSource();
        return source.map(Path::toFile).orElseThrow(() -> new RuntimeException("wHaT?"));
    }

    @Override
    protected String getPluginFolder() {
        return "mods/";
    }

    @Override
    public InputStream getResource(final String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Server getServer() {
        return game.getServer();
    }
}
