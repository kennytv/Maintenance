/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.sponge.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

public final class SpongeSenderInfo implements SenderInfo {
    private final CommandSource sender;

    public SpongeSenderInfo(final CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public UUID getUuid() {
        return sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String message) {
        sender.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public void sendMessage(final Text text) {
        sender.sendMessage(text);
    }
}
