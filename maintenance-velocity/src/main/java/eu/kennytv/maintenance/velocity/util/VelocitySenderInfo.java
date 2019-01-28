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

package eu.kennytv.maintenance.velocity.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.ComponentSerializers;

import java.util.UUID;

public final class VelocitySenderInfo implements SenderInfo {
    private final CommandSource sender;

    public VelocitySenderInfo(final CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public UUID getUuid() {
        return sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender instanceof Player ? ((Player) sender).getUsername() : null;
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String message) {
        sender.sendMessage(ComponentSerializers.LEGACY.deserialize(message));
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public void sendMessage(final TextComponent textComponent) {
        sender.sendMessage(textComponent);
    }
}
