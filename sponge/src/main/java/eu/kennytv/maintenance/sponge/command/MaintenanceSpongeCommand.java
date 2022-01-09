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
package eu.kennytv.maintenance.sponge.command;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MaintenanceSpongeCommand extends MaintenanceCommand implements Command.Raw {
    private final MaintenanceSpongePlugin plugin;
    private static final String[] EMPTY = new String[0];
    private static final String[] EMPTY_SINGLE = {""};

    public MaintenanceSpongeCommand(final MaintenanceSpongePlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
        registerCommands();
    }

    @Override
    public CommandResult process(final CommandCause cause, final ArgumentReader.Mutable argument) {
        final String input = argument.input();
        execute(new SpongeSenderInfo(cause), input.isEmpty() ? EMPTY : input.split(" ", 0));
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(final CommandCause cause, final ArgumentReader.Mutable argument) {
        final String input = argument.input();
        return getSuggestions(new SpongeSenderInfo(cause), input.isEmpty() ? EMPTY_SINGLE : input.split(" ", -1)).stream().map(CommandCompletion::of).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(final CommandCause cause) {
        return plugin.hasPermission(cause, "command");
    }

    @Override
    public Optional<Component> shortDescription(final CommandCause cause) {
        return Optional.of(Component.text("Maintenance main-command"));
    }

    @Override
    public Optional<Component> extendedDescription(final CommandCause cause) {
        return shortDescription(cause);
    }

    @Override
    public Optional<Component> help(final CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Component usage(final CommandCause cause) {
        return null;
    }

    @Override
    public void sendDumpMessage(final SenderInfo sender, final String url) {
        final SpongeSenderInfo spongeSender = ((SpongeSenderInfo) sender);
        spongeSender.send(Component.text().append(plugin.translate(plugin.getPrefix() + "§7Click here to copy the link")).clickEvent(ClickEvent.suggestCommand(url))
                .hoverEvent(HoverEvent.showText(plugin.translate("§aClick here to copy the link"))).build());
    }

    @Override
    public List<String> getPlayersCompletion() {
        final List<String> list = new ArrayList<>();
        for (final Player player : plugin.getServer().onlinePlayers()) {
            list.add(player.name());
        }
        return list;
    }
}
