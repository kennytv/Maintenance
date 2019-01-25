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

package eu.kennytv.maintenance.bungee.command;

import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class MaintenanceBungeeCommand extends MaintenanceProxyCommand {
    private final MaintenanceBungeePlugin plugin;

    public MaintenanceBungeeCommand(final MaintenanceBungeePlugin plugin, final SettingsBungee settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Override
    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the proxy to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            final TextComponent tc = new TextComponent("§6× §8[§aUpdate§8]");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maintenance forceupdate"));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§aClick here to update the plugin")));
            tc.addExtra(" §8< §7Or use the command §c/maintenance forceupdate");

            ((BungeeSenderInfo) sender).sendMessage(tc);
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }

    @Override
    protected List<String> getServersCompletion(final String s) {
        return plugin.getProxy().getServers().entrySet().stream().filter(entry -> entry.getKey().toLowerCase().startsWith(s))
                .filter(entry -> !plugin.isMaintenance(entry.getValue())).map(entry -> entry.getValue().getName()).collect(Collectors.toList());
    }

    @Override
    protected String getServer(final SenderInfo sender) {
        final ProxiedPlayer player = plugin.getProxy().getPlayer(sender.getUuid());
        return player != null ? player.getServer().getInfo().getName() : "";
    }
}
