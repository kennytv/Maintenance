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

package eu.kennytv.maintenance.velocity.command;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public final class MaintenanceVelocityCommand extends MaintenanceCommand implements Command {

    public MaintenanceVelocityCommand(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @Override
    public void execute(final CommandSource commandSource, final @NonNull String[] strings) {
        execute(new VelocitySenderInfo(commandSource), strings);
    }

    @Override
    public List<String> suggest(final CommandSource source, final @NonNull String[] currentArgs) {
        return getSuggestions(new VelocitySenderInfo(source), currentArgs);
    }

    @Override
    public boolean hasPermission(final CommandSource source, final @NonNull String[] args) {
        return source.hasPermission("maintenance.command");
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {

    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {

    }
}
