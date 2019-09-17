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

package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;

public abstract class CommandInfo {
    protected final MaintenancePlugin plugin;
    private final String helpMessageKey;
    private final String permission;

    protected CommandInfo(final MaintenancePlugin plugin, final String permission) {
        this.plugin = plugin;
        this.permission = permission;
        // Just take the class name as the language key
        this.helpMessageKey = "help" + getClass().getSimpleName().replace("Command", "");
    }

    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission(permission);
    }

    public String getHelpMessage() {
        return getMessage(helpMessageKey);
    }

    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        return Collections.emptyList();
    }

    public abstract void execute(SenderInfo sender, String[] args);

    protected boolean checkPermission(final SenderInfo sender, final String permission) {
        if (!sender.hasMaintenancePermission(permission)) {
            sender.sendMessage(getMessage("noPermission"));
            return true;
        }
        return false;
    }

    protected boolean checkArgs(final SenderInfo sender, final String[] args, final int length) {
        if (args.length != length) {
            sender.sendMessage(getHelpMessage());
            return true;
        }
        return false;
    }

    protected String getMessage(final String s) {
        return plugin.getSettings().getMessage(s);
    }

    protected Settings getSettings() {
        return plugin.getSettings();
    }
}
