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
package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.api.proxy.DummyServer;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SingleToggleCommand extends ProxyCommandInfo {

    public SingleToggleCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, null);
    }

    @Override
    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission("toggle") || sender.hasPermission("maintenance.singleserver.toggle");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 0 || args.length > 3) {
            sender.send(getHelpMessage());
            return;
        }

        final boolean maintenance = args[0].equalsIgnoreCase("on");
        if (args.length == 1) {
            toggleGlobal(sender, maintenance, null);
            return;
        }

        final String server = args[1];
        final String mode = args.length == 3 ? args[2] : null;
        if (server.equalsIgnoreCase("global")) {
            if (!maintenance && args.length == 3) {
                sender.send(getHelpMessage());
                return;
            }

            toggleGlobal(sender, maintenance, mode);
            return;
        }

        if (!maintenance && args.length != 2) {
            sender.send(getHelpMessage());
            return;
        }

        toggleServer(sender, maintenance, server, mode);
    }

    private void toggleGlobal(final SenderInfo sender, final boolean maintenance, final String mode) {
        if (checkPermission(sender, "toggle")) {
            return;
        }

        if (maintenance && plugin.isMaintenance()) {
            if (mode != null) {
                plugin.getSettings().setActiveMode(mode);
                return;
            }

            sender.send(getMessage("alreadyEnabled"));
            return;
        }

        if (maintenance == plugin.isMaintenance()) {
            sender.send(getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
            return;
        }

        plugin.setMaintenance(maintenance, mode);
    }

    private void toggleServer(final SenderInfo sender, final boolean maintenance, final String serverArg, final String mode) {
        if (checkPermission(sender, "singleserver.toggle")) {
            return;
        }

        Server server = plugin.getServer(serverArg);
        if (server == null) {
            if (maintenance) {
                sender.send(getMessage("serverNotFound"));
                return;
            }
            // Let servers be removed from the maintenance list, even if they don't exist on the proxy
            server = new DummyServer(serverArg);
            if (!plugin.isMaintenance(server)) {
                sender.send(getMessage("serverNotFound"));
                return;
            }
        }

        if (maintenance && plugin.isMaintenance(server) && mode != null) {
            if (plugin.setMaintenanceModeToServer(server, mode)) {
                return;
            }
            sender.send(getMessage("singleServerAlreadyEnabled", "%SERVER%", server.getName()));
            return;
        }

        if (plugin.setMaintenanceToServer(server, maintenance, mode)) {
            if (!sender.isPlayer() || !server.getName().equals(plugin.getServerNameOf(sender))) {
                sender.send(getMessage(maintenance ? "singleMaintenanceActivated" : "singleMaintenanceDeactivated", "%SERVER%", server.getName()));
            }
        } else {
            sender.send(getMessage(maintenance ? "singleServerAlreadyEnabled" : "singleServerAlreadyDisabled", "%SERVER%", server.getName()));
        }
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            if (sender.hasMaintenancePermission("toggle") && args[0].equalsIgnoreCase("on")) {
                suggestions.add("global");
            }

            if (!sender.hasMaintenancePermission("singleserver.toggle")) {
                return suggestions;
            }

            final List<String> serverSuggestions = args[0].equalsIgnoreCase("off")
                    ? plugin.getCommandManager().getMaintenanceServersCompletion(args[1].toLowerCase(Locale.ROOT))
                    : plugin.getCommandManager().getServersCompletion(args[1].toLowerCase(Locale.ROOT));
            suggestions.addAll(serverSuggestions);
            return suggestions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("on") && (sender.hasMaintenancePermission("toggle") || sender.hasMaintenancePermission("singleserver.toggle"))) {
            final List<String> modes = new ArrayList<>(getSettings().getPingMessages().getKeys());
            modes.remove("default");
            return modes;
        }

        return Collections.emptyList();
    }
}
