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

package eu.kennytv.maintenance.velocity.command;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.SettingsVelocity;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MaintenanceVelocityCommand extends MaintenanceProxyCommand implements Command {
    private final MaintenanceVelocityPlugin plugin;

    public MaintenanceVelocityCommand(final MaintenanceVelocityPlugin plugin, final SettingsVelocity settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSource commandSource, final @NonNull String[] strings) {
        execute(new VelocitySenderInfo(commandSource), strings);
    }

    @Override
    public List<String> suggest(final CommandSource source, final @NonNull String[] currentArgs) {
        return getSuggestions(new VelocitySenderInfo(source), currentArgs);
    }

    @Override
    public boolean hasPermission(final CommandSource source, final @NonNull String[] args) {
        return source.hasPermission("maintenance.command");
    }

    @Override
    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the proxy to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            final TextComponent tc = TextComponent.of("§6× §8[§aUpdate§8]");
            tc.clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maintenance forceupdate"));
            tc.hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("§aClick here to update the plugin")));
            tc.append(TextComponent.of(" §8< §7Or use the command §c/maintenance forceupdate"));
            ((VelocitySenderInfo) sender).sendMessage(tc);
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }

    @Override
    protected List<String> getServersCompletion(final String s) {
        return plugin.getServer().getAllServers().stream().filter(server -> server.getServerInfo().getName().toLowerCase().startsWith(s))
                .filter(server -> !plugin.isMaintenance(server.getServerInfo())).map(server -> server.getServerInfo().getName()).collect(Collectors.toList());
    }

    @Override
    protected List<String> getPlayersCompletion() {
        return plugin.getServer().getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
    }

    @Override
    protected String getServer(final SenderInfo sender) {
        final Optional<Player> player = plugin.getServer().getPlayer(sender.getUuid());
        return player.map(p -> p.getCurrentServer().get().getServerInfo().getName()).orElse("");
    }
}
