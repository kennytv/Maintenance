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
import eu.kennytv.maintenance.api.sponge.MaintenanceSpongeAPI;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.MaintenanceVersion;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.sponge.command.MaintenanceSpongeCommand;
import eu.kennytv.maintenance.sponge.listener.ClientConnectionListener;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import eu.kennytv.maintenance.sponge.util.LoggerWrapper;
import eu.kennytv.maintenance.sponge.util.SpongeOfflinePlayerInfo;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import eu.kennytv.maintenance.sponge.util.SpongeTask;
import org.bstats.sponge.Metrics2;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
@Plugin(id = "maintenancesponge", name = "MaintenanceSponge", version = MaintenanceVersion.VERSION, authors = "KennyTV",
        description = "Enable maintenance mode with a custom maintenance motd and icon.", url = "https://www.spigotmc.org/resources/maintenance.40699/",
        dependencies = @Dependency(id = "serverlistplus", optional = true))
public final class MaintenanceSpongePlugin extends MaintenancePlugin {
    private Logger logger;
    private Favicon favicon;
    @Inject
    private Game game;
    @Inject
    private PluginContainer container;
    @Inject
    private Metrics2 metrics;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;

    @Inject
    public MaintenanceSpongePlugin() {
        super(MaintenanceVersion.VERSION, ServerType.SPONGE);
    }

    @Listener
    public void onEnable(final GameInitializationEvent event) {
        logger = new LoggerWrapper(container.getLogger());

        settings = new Settings(this, "mysql", "proxied-maintenance-servers", "fallback", "playercountmessage", "enable-playercountmessage");

        sendEnableMessage();

        final MaintenanceSpongeCommand command = new MaintenanceSpongeCommand(this, settings);
        commandManager = command;
        game.getCommandManager().register(this, command, "maintenance", "maintenancesponge");
        final EventManager em = game.getEventManager();
        em.registerListeners(this, new ClientPingServerListener(this, settings));
        em.registerListeners(this, new ClientConnectionListener(this, settings));
        em.registerListeners(this, new ClientPingServerListener(this, settings));

        // ServerListPlus integration
        game.getPluginManager().getPlugin("serverlistplus").ifPresent(slpContainer -> slpContainer.getInstance().ifPresent(serverListPlus -> {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            serverListPlusHook.setEnabled(!settings.isMaintenance());
            logger.info("Enabled ServerListPlus integration!");
        }));
    }

    @Listener
    public void reload(final GameReloadEvent event) {
        settings.reloadConfigs();
        logger.info("Reloaded config files!");
    }

    @Deprecated
    public static IMaintenance getAPI() {
        return MaintenanceSpongeAPI.getAPI();
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
        getServer().getBroadcastChannel().send(translate(message));
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        Text text;
        try {
            text = Text.builder(getPrefix())
                    .append(translate("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/ "))
                    .append(Text.builder("§7§l§o(CLICK ME)")
                            .onClick(TextActions.openUrl(new URL("https://www.spigotmc.org/resources/maintenance.40699/")))
                            .onHover(TextActions.showText(translate("§7§l§o(CLICK ME)"))).build()).build();
        } catch (final MalformedURLException e) {
            text = translate("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/");
            e.printStackTrace();
        }
        ((SpongeSenderInfo) sender).sendMessage(text);
    }

    @Override
    public SenderInfo getOfflinePlayer(final String name) {
        final UserStorageService userStorage = game.getServiceManager().provide(UserStorageService.class).get();
        return userStorage.get(name).map(SpongeOfflinePlayerInfo::new).orElse(null);
    }

    @Override
    public SenderInfo getOfflinePlayer(final UUID uuid) {
        final UserStorageService userStorage = game.getServiceManager().provide(UserStorageService.class).get();
        return userStorage.get(uuid).map(SpongeOfflinePlayerInfo::new).orElse(null);
    }

    @Override
    protected void kickPlayers() {
        getServer().getOnlinePlayers().stream()
                .filter(p -> !hasPermission(p, "bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId()))
                .forEach(p -> p.kick(Text.of(settings.getKickMessage())));
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
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

    @Override
    public String getServerVersion() {
        return game.getPlatform().getImplementation().getName();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return game.getPluginManager().getPlugins().stream().map(plugin ->
                new PluginDump(plugin.getId() + "/" + plugin.getName(), plugin.getVersion().orElse("-"), plugin.getAuthors())).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws IOException {
        favicon = game.getRegistry().loadFavicon(ImageIO.read(file));
    }

    public boolean hasPermission(final CommandSource sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    public Server getServer() {
        return game.getServer();
    }

    public Favicon getFavicon() {
        return favicon;
    }

    public Text translate(final String s) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(s);
    }
}
