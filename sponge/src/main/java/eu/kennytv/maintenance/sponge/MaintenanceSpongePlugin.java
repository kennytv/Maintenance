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
package eu.kennytv.maintenance.sponge;

import com.google.inject.Inject;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.LuckPermsHook;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.util.MaintenanceVersion;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.sponge.command.MaintenanceSpongeCommand;
import eu.kennytv.maintenance.sponge.listener.ClientConnectionListener;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import eu.kennytv.maintenance.sponge.util.ComponentUtil;
import eu.kennytv.maintenance.sponge.util.LoggerWrapper;
import eu.kennytv.maintenance.sponge.util.SpongePlayer;
import eu.kennytv.maintenance.sponge.util.SpongeTask;
import eu.kennytv.maintenance.sponge.util.SpongeUser;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginContributor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin("maintenance")
public final class MaintenanceSpongePlugin extends MaintenancePlugin {

    @SuppressWarnings("SpongeLogging")
    private final Logger logger;
    private final PluginContainer container;
    private final Game game;
    private Favicon favicon;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path dataFolder;

    @SuppressWarnings("SpongeInjection")
    @Inject
    public MaintenanceSpongePlugin(final PluginContainer container, final Game game, final org.apache.logging.log4j.Logger logger, final Metrics.Factory metricsFactory) {
        super(MaintenanceVersion.VERSION, ServerType.SPONGE);
        this.container = container;
        this.game = game;
        this.logger = new LoggerWrapper(logger);
        metricsFactory.make(16501);
    }

    @Listener
    public void onEnable(final StartingEngineEvent<Server> event) {
        settings = new Settings(this, "mysql", "proxied-maintenance-servers", "fallback", "waiting-server",
                "playercountmessage");

        sendEnableMessage();

        final MaintenanceSpongeCommand command = new MaintenanceSpongeCommand(this, settings);
        commandManager = command;
        game.server().commandManager().registrar(Command.Raw.class).get().register(container, command, "maintenance", "maintenancesponge", "mt");
        final EventManager em = game.eventManager();
        em.registerListeners(container, new ClientPingServerListener(this, settings));
        em.registerListeners(container, new ClientConnectionListener(this, settings));

        continueLastEndtimer();

        final PluginManager pluginManager = game.pluginManager();
        pluginManager.plugin("serverlistplus").map(PluginContainer::instance).ifPresent(serverListPlus -> {
            serverListPlusHook = new ServerListPlusHook(serverListPlus);
            if (settings.isEnablePingMessages()) {
                serverListPlusHook.setEnabled(!settings.isMaintenance());
            }
            logger.info("Enabled ServerListPlus integration!");
        });
        if (false && pluginManager.plugin("luckperms").isPresent()) {
            LuckPermsHook.<Subject>register(this);
            logger.info("Registered LuckPerms context");
        }
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        disable();
    }

    //TODO hello?
    /*@Listener
    public void reload(final GameReloadEvent event) {
        settings.reloadConfigs();
        logger.info("Reloaded config files!");
    }*/

    @Override
    public Task startMaintenanceRunnable(final Runnable runnable) {
        final org.spongepowered.api.scheduler.Task task = org.spongepowered.api.scheduler.Task.builder().plugin(container).execute(runnable).interval(1, TimeUnit.SECONDS).build();
        return new SpongeTask(game.server().scheduler().submit(task));
    }

    @Override
    public void async(final Runnable runnable) {
        final org.spongepowered.api.scheduler.Task task = org.spongepowered.api.scheduler.Task.builder().plugin(container).execute(runnable).build();
        game.asyncScheduler().submit(task);
    }

    @Override
    protected void executeConsoleCommand(final String command) {
        try {
            game.server().commandManager().process(command);
        } catch (final CommandException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void broadcast(final Component component) {
        game.server().sendMessage(ComponentUtil.toSponge(component));
    }

    @Override
    public void sendUpdateNotification(final SenderInfo sender) {
        try {
            net.kyori.adventure.text.Component component = net.kyori.adventure.text.Component.text().append(translate(getPrefix()))
                    .append(translate("§cDownload it at: §6https://www.spigotmc.org/resources/maintenance.40699/ "))
                    .append(translate("§7§l§o(CLICK ME)"))
                    .clickEvent(ClickEvent.openUrl(new URL("https://www.spigotmc.org/resources/maintenance.40699/")))
                    .hoverEvent(HoverEvent.showText(translate("§7§l§o(CLICK ME)"))).build();
            ((SpongePlayer) sender).send(component);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getOfflinePlayer(final String name, final Consumer<@Nullable SenderInfo> consumer) {
        final Optional<ServerPlayer> player = game.server().player(name);
        if (player.isPresent()) {
            consumer.accept(new SpongePlayer(player.get()));
        } else {
            game.server().userManager().load(name).whenComplete((optional, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                    consumer.accept(null);
                } else {
                    consumer.accept(optional.map(SpongeUser::new).orElse(null));
                }
            });
        }
    }

    @Override
    public void getOfflinePlayer(final UUID uuid, final Consumer<@Nullable SenderInfo> consumer) {
        final Optional<ServerPlayer> player = game.server().player(uuid);
        if (player.isPresent()) {
            consumer.accept(new SpongePlayer(player.get()));
        } else {
            game.server().userManager().load(uuid).whenComplete((optional, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                    consumer.accept(null);
                } else {
                    consumer.accept(optional.map(SpongeUser::new).orElse(null));
                }
            });
        }
    }

    @Override
    protected void kickPlayers() {
        final net.kyori.adventure.text.Component component = ComponentUtil.toSponge(settings.getKickMessage());
        for (final ServerPlayer player : getServer().onlinePlayers()) {
            if (!hasPermission(player, "bypass") && !settings.isWhitelisted(player.uniqueId())) {
                player.kick(component);
            }
        }
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }

    @Override
    public File getPluginFile() {
        throw new UnsupportedOperationException("grumbles");
        /*final Optional<Path> source = container.getSource();
        return source.map(Path::toFile).orElseThrow(() -> new RuntimeException("wHaT?"));*/
    }

    @Override
    protected int getOnlinePlayers() {
        return getServer().onlinePlayers().size();
    }

    @Override
    protected int getMaxPlayers() {
        return getServer().maxPlayers();
    }

    @Override
    protected String getPluginFolder() {
        return "mods/";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getServerVersion() {
        return game.platform().container(Platform.Component.IMPLEMENTATION).toString();
    }

    @Override
    public List<PluginDump> getPlugins() {
        return game.pluginManager().plugins().stream().map(plugin -> {
            final PluginMetadata metadata = plugin.metadata();
            final List<String> contributors = metadata.contributors().stream().map(PluginContributor::name).collect(Collectors.toList());
            final String id = metadata.id();
            return new PluginDump(id + "/" + metadata.name().orElse(id), metadata.version().toString(), contributors);
        }).collect(Collectors.toList());
    }

    @Override
    protected void loadIcon(final File file) throws IOException {
        favicon = Favicon.load(ImageIO.read(file));
    }

    public boolean hasPermission(final Subject sender, final String permission) {
        return sender.hasPermission("maintenance." + permission) || sender.hasPermission("maintenance.admin");
    }

    public Server getServer() {
        return game.server();
    }

    public Favicon getFavicon() {
        return favicon;
    }

    @Deprecated
    public net.kyori.adventure.text.Component translate(final String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
