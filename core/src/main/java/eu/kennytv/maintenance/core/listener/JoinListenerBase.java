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
package eu.kennytv.maintenance.core.listener;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.TextComponent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.event.ClickEvent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.event.HoverEvent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.format.NamedTextColor;
import eu.kennytv.maintenance.lib.kyori.adventure.text.format.TextDecoration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class JoinListenerBase {
    protected final MaintenancePlugin plugin;
    protected final Settings settings;
    private final Set<UUID> notifiedPlayers = new HashSet<>();

    protected JoinListenerBase(final MaintenancePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    /**
     * Deals with the login and returns true if the player should be kicked after this method.
     *
     * @param sender wrapper of the joining player
     * @return true if the sender should be kicked
     */
    protected boolean shouldKick(final SenderInfo sender, final boolean updateCheck) {
        if (!settings.isMaintenance() || sender.hasMaintenancePermission("bypass") || settings.isWhitelisted(sender.getUuid())) {
            if (updateCheck) {
                updateCheck(sender);
            }
            return false;
        }

        return true;
    }

    protected boolean shouldKick(final SenderInfo sender) {
        return shouldKick(sender, true);
    }

    protected void updateCheck(final SenderInfo sender) {
        if (!settings.hasUpdateChecks()) return;
        if (!sender.hasPermission("maintenance.admin") || notifiedPlayers.contains(sender.getUuid())) return;
        plugin.async(() -> {
            if (!plugin.updateAvailable()) {
                return;
            }

            notifiedPlayers.add(sender.getUuid());
            sender.sendMessage(plugin.getPrefix() + "§cThere is a newer version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());

            final TextComponent text = Component.text().content("Download it at: ").color(NamedTextColor.RED)
                    .append(Component.text().content(MaintenancePlugin.HANGAR_URL).color(NamedTextColor.GOLD))
                    .append(Component.text().content(" (CLICK ME)").color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, MaintenancePlugin.HANGAR_URL))
                    .hoverEvent(HoverEvent.showText(Component.text("Download the latest version").color(NamedTextColor.GREEN)))
                    .build();
            sender.send(plugin.prefix().append(text));
        });
    }

    protected abstract void broadcastJoinNotification(String name);
}
