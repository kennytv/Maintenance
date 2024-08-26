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
package eu.kennytv.maintenance.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class MaintenanceVelocityCommand extends MaintenanceProxyCommand implements SimpleCommand {
    private final MaintenanceVelocityPlugin plugin;

    public MaintenanceVelocityCommand(final MaintenanceVelocityPlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
        registerCommands();
    }

    @Override
    public void execute(final Invocation invocation) {
        execute(new VelocitySenderInfo(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        return getSuggestions(new VelocitySenderInfo(invocation.source()), invocation.arguments());
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        final CommandSource source = invocation.source();
        return source.hasPermission("maintenance.admin") || source.hasPermission("maintenance.command");
    }

    @Override
    protected void sendUpdateMessage(final SenderInfo sender) {
        final TextComponent tc = Component.text("").append(plugin.translate("§6× §8[§aUpdate§8]"))
                .clickEvent(ClickEvent.runCommand("/maintenance forceupdate"))
                .hoverEvent(HoverEvent.showText(plugin.translate("§aClick here to update the plugin")))
                .append(plugin.translate(" §8(§7Or use the command §c/maintenance forceupdate§8)"));
        ((VelocitySenderInfo) sender).sendMessage(tc);
    }

    @Override
    public List<String> getServersCompletion(final String s) {
        final List<String> list = new ArrayList<>();
        for (final RegisteredServer server : plugin.getServer().getAllServers()) {
            final String name = server.getServerInfo().getName();
            if (name.toLowerCase().startsWith(s) && !plugin.getSettingsProxy().getMaintenanceServers().contains(name)) {
                list.add(name);
            }
        }
        return list;
    }

    @Override
    public List<String> getPlayersCompletion() {
        final List<String> list = new ArrayList<>();
        for (final Player player : plugin.getServer().getAllPlayers()) {
            list.add(player.getUsername());
        }
        return list;
    }
}
