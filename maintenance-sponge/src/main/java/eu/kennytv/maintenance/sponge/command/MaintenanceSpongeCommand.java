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

package eu.kennytv.maintenance.sponge.command;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NonnullByDefault
public final class MaintenanceSpongeCommand extends MaintenanceCommand implements CommandCallable {
    private final MaintenanceSpongePlugin plugin;
    private static final String[] EMPTY = new String[0];

    public MaintenanceSpongeCommand(final MaintenanceSpongePlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(final CommandSource source, final String arguments) throws CommandException {
        execute(new SpongeSenderInfo(source), getArgs(arguments, 0));
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(final CommandSource source, final String arguments, @Nullable final Location<World> targetPosition) throws CommandException {
        return getSuggestions(new SpongeSenderInfo(source), getArgs(arguments, -1));
    }

    @Override
    public boolean testPermission(final CommandSource source) {
        return source.hasPermission("maintenance.command");
    }

    @Override
    public Optional<Text> getShortDescription(final CommandSource source) {
        return Optional.of(Text.of("Maintenance main-command"));
    }

    @Override
    public Optional<Text> getHelp(final CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(final CommandSource source) {
        return null;
    }

    @Override
    protected void sendDumpMessage(final SenderInfo sender, final String url) {
        final SpongeSenderInfo spongeSender = ((SpongeSenderInfo) sender);
        spongeSender.sendMessage(plugin.translate(plugin.getPrefix() + "§c" + url));
        if (spongeSender.isPlayer()) {
            spongeSender.sendMessage(Text.builder(plugin.getPrefix() + "§7Click here to copy the link").onClick(TextActions.suggestCommand(url))
                    .onHover(TextActions.showText(plugin.translate("§aClick here to copy the link"))).build());
        }
    }

    @Override
    protected List<String> getPlayersCompletion() {
        return Sponge.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private String[] getArgs(final String arguments, final int limit) {
        return arguments.isEmpty() ? EMPTY : arguments.split(" ", limit);
    }
}
