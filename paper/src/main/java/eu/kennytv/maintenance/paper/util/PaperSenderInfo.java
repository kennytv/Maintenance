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
package eu.kennytv.maintenance.paper.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PaperSenderInfo implements SenderInfo {
    private final CommandSender sender;

    public PaperSenderInfo(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public UUID uuid() {
        return sender instanceof Player player ? player.getUniqueId() : null;
    }

    @Override
    public String name() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void send(final Component component) {
        sender.sendMessage(component);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }
}
