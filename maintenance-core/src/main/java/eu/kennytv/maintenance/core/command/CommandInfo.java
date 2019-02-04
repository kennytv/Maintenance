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

import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;

public final class CommandInfo {
    private final String[] messages;
    private final String permission;
    private final TabCompleteCallback tabCompleteCallback;

    CommandInfo(final String permission, final TabCompleteCallback tabCompleteCallback, final String... messages) {
        this.messages = messages;
        this.tabCompleteCallback = tabCompleteCallback;
        this.permission = permission;
    }

    CommandInfo(final String permission, final String... messages) {
        this(permission, null, messages);
    }

    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission(permission);
    }

    public String[] getMessages() {
        return messages;
    }

    public List<String> getTabCompletion(final String[] args) {
        return tabCompleteCallback == null ? Collections.emptyList() : tabCompleteCallback.tabComplete(args);
    }

    @FunctionalInterface
    public interface TabCompleteCallback {

        List<String> tabComplete(String[] args);
    }
}
