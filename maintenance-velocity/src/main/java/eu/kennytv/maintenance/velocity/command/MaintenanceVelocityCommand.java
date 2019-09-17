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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class MaintenanceVelocityCommand extends MaintenanceProxyCommand implements Command {
    private final MaintenanceVelocityPlugin plugin;

    public MaintenanceVelocityCommand(final MaintenanceVelocityPlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
        registerCommands();
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
        return true;
    }

    @Override
    protected void sendUpdateMessage(final SenderInfo sender) {
        final TextComponent tc = TextComponent.builder("").append(plugin.translate("§6× §8[§aUpdate§8]"))
                .clickEvent(ClickEvent.runCommand("/maintenance forceupdate"))
                .hoverEvent(HoverEvent.showText(plugin.translate("§aClick here to update the plugin")))
                .append(plugin.translate(" §8(§7Or use the command §c/maintenance forceupdate§8)")).build();
        ((VelocitySenderInfo) sender).sendMessage(tc);
    }

    @Override
    public void sendDumpMessage(final SenderInfo sender, final String url) {
        final TextComponent clickText = TextComponent.builder("").append(plugin.translate(plugin.getPrefix() + "§7Click here to copy the link."))
                .clickEvent(ClickEvent.suggestCommand(url))
                .hoverEvent(HoverEvent.showText(plugin.translate("§aClick here to copy the link"))).build();
        ((VelocitySenderInfo) sender).sendMessage(clickText);
    }

    @Override
    public List<String> getServersCompletion(final String s) {
        final List<String> list = new ArrayList<>();
        for (final RegisteredServer server : plugin.getServer().getAllServers()) {
            final String name = server.getServerInfo().getName();
            if (name.toLowerCase().startsWith(s) && !plugin.getSettingsProxy().getFallbackServer().contains(name)) {
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
