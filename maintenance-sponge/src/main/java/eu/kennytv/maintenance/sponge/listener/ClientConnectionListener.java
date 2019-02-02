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

package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class ClientConnectionListener extends JoinListenerBase {

    public ClientConnectionListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @Listener
    public void login(final ClientConnectionEvent.Login event) {
        if (handleLogin(new SpongeSenderInfo(event.getTargetUser().getPlayer().get()), false)) {
            event.setCancelled(true);
            event.setMessage(Text.of(settings.getKickMessage()));
        }
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        final Text text = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName()));
        Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification")).forEach(p -> p.sendMessage(text));
    }
}
