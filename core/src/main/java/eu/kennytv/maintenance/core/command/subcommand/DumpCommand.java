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
package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class DumpCommand extends CommandInfo {
    private long lastDump;

    public DumpCommand(final MaintenancePlugin plugin) {
        super(plugin, "dump");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        if (System.currentTimeMillis() - lastDump < TimeUnit.MINUTES.toMillis(5)) {
            sender.sendPrefixedRich("<red>You can only create a dump every 5 minutes!");
            return;
        }

        lastDump = System.currentTimeMillis();
        sender.sendPrefixedRich("<gray>The dump is being created, this might take a moment.");
        plugin.async(() -> {
            final String key = plugin.pasteDump();
            if (key == null) {
                if (sender.isPlayer()) {
                    sender.sendPrefixedRich("<red>Could not paste dump (see the console for details)");
                }
                return;
            }

            final String url = "https://pastes.dev/" + key;
            sender.sendPrefixedRich("<red><click:open_url:'" + url + "'>" + url + "</click>");
            if (sender.isPlayer()) {
                final TextComponent text = Component.text().content("Click here to copy the link").color(NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, url))
                        .hoverEvent(HoverEvent.showText(Component.text("Click here to copy the link to your clipboard").color(NamedTextColor.GREEN)))
                        .build();
                sender.send(plugin.prefix().append(text));
            }
        });
    }
}
