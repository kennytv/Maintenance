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
import java.util.UUID;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class SpongePlayer implements SenderInfo {
    private final ServerPlayer player;

    public SpongePlayer(final ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return player.uniqueId();
    }

    @Override
    public String getName() {
        return player.name();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void send(final Component component) {
        player.sendMessage(ComponentUtil.toSponge(component));
    }

    public void send(final net.kyori.adventure.text.Component component) {
        player.sendMessage(component);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
