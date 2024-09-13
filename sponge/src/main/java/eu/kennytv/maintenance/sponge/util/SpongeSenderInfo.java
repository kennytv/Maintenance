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
package eu.kennytv.maintenance.sponge.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

public final class SpongeSenderInfo implements SenderInfo {
    private final CommandCause cause;

    public SpongeSenderInfo(final CommandCause cause) {
        this.cause = cause;
    }

    @Override
    public UUID getUuid() {
        return cause instanceof Player ? ((Identifiable) cause).uniqueId() : null;
    }

    @Override
    public String getName() {
        return cause.friendlyIdentifier().orElse(cause.identifier());
    }

    @Override
    public boolean hasPermission(final String permission) {
        return cause.hasPermission(permission);
    }

    @Override
    public void send(final Component component) {
        cause.sendMessage(Identity.nil(), ComponentUtil.toSponge(component));
    }

    public void send(final net.kyori.adventure.text.Component component) {
        cause.sendMessage(Identity.nil(), component);
    }

    @Override
    public boolean isPlayer() {
        return cause instanceof Player;
    }
}
