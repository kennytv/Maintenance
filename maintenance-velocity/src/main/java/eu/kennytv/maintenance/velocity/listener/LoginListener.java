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

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import net.kyori.text.TextComponent;

public final class LoginListener extends JoinListenerBase {
    private final MaintenanceVelocityPlugin plugin;

    public LoginListener(final MaintenanceVelocityPlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Subscribe
    public void login(final LoginEvent event) {
        if (!event.getResult().isAllowed()) return;
        if (kickPlayer(new VelocitySenderInfo(event.getPlayer()), false))
            event.setResult(ResultedEvent.ComponentResult.denied(plugin.translate(settings.getKickMessage())));
    }

    @Subscribe
    public void postLogin(final PostLoginEvent event) {
        updateCheck(new VelocitySenderInfo(event.getPlayer()));
    }

    @Override
    protected void broadcastJoinNotification(final String name) {
        final TextComponent s = plugin.translate(settings.getMessage("joinNotification").replace("%PLAYER%", name));
        plugin.getServer().getAllPlayers().stream().filter(p -> plugin.hasPermission(p, "joinnotification")).forEach(p -> p.sendMessage(s));
    }
}
