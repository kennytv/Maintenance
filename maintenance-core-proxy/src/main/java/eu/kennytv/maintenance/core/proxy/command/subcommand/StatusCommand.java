/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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

package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

public final class StatusCommand extends ProxyCommandInfo {

    public StatusCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, "singleserver.status");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (getSettings().getMaintenanceServers().isEmpty()) {
            sender.sendMessage(getMessage("singleServerMaintenanceListEmpty"));
        } else {
            sender.sendMessage(getMessage("singleServerMaintenanceList"));
            for (final String server : getSettings().getMaintenanceServers()) {
                sender.sendMessage("§8- §b" + server);
            }
        }
    }
}
