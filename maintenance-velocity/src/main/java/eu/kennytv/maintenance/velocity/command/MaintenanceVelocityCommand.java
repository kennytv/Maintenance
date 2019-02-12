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
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

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
        final TextComponent tc = plugin.translate("§6× §8[§aUpdate§8]");
        tc.clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maintenance forceupdate"));
        tc.hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, plugin.translate("§aClick here to update the plugin")));
        tc.append(plugin.translate(" §8< §7Or use the command §c/maintenance forceupdate"));
        ((VelocitySenderInfo) sender).sendMessage(tc);
    }

    @Override
    public void sendDumpMessage(final SenderInfo sender, final String url) {
        sender.sendMessage(plugin.getPrefix() + "§c" + url);
        if (sender.isPlayer()) {
            final TextComponent clickText = plugin.translate(plugin.getPrefix() + "§7Click here to copy the link)");
            clickText.clickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, url));
            clickText.hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, plugin.translate("§aClick here to copy the link")));
            ((VelocitySenderInfo) sender).sendMessage(clickText);
        }
    }

    @Override
    public List<String> getServersCompletion(final String s) {
        return plugin.getServer().getAllServers().stream().filter(server -> server.getServerInfo().getName().toLowerCase().startsWith(s))
                .filter(server -> !plugin.getSettingsProxy().getFallbackServer().contains(server.getServerInfo().getName()))
                .map(server -> server.getServerInfo().getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getPlayersCompletion() {
        return plugin.getServer().getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
    }
}
