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

package eu.kennytv.maintenance.velocity.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import net.kyori.text.TextComponent;

public final class LoginListener extends JoinListenerBase implements EventHandler<LoginEvent> {
    private final MaintenanceVelocityPlugin plugin;

    public LoginListener(final MaintenanceVelocityPlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Override
    public void execute(final LoginEvent event) {
        if (handleLogin(new VelocitySenderInfo(event.getPlayer()), false))
            event.setResult(ResultedEvent.ComponentResult.denied(plugin.translate(settings.getKickMessage().replace("%NEWLINE%", "\n"))));
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        final TextComponent s = plugin.translate(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName()));
        plugin.getServer().getAllPlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification")).forEach(p -> p.sendMessage(s));
    }
}
