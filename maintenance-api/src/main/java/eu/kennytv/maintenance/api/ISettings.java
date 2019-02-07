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

package eu.kennytv.maintenance.api;

import java.util.Map;
import java.util.UUID;

/**
 * @author KennyTV
 * @since 2.1
 */
public interface ISettings {

    /**
     * Note that the value might need a few seconds to update, if you're on BungeeCord with MySQL enabled
     *
     * @return true if maintenance is currently enabled
     */
    boolean isMaintenance();

    /**
     * @return value of the 'send-join-notification' config field
     */
    boolean isJoinNotifications();

    /**
     * @return value of the 'custom-maintenance-icon' config field
     */
    boolean hasCustomIcon();

    /**
     * Returns a map of the currently maintenance-whitelisted players as their uuid mapped to their saved name.
     * Note that the names might be incorrect/outdated.
     *
     * @return map of uuids of whitelisted players with their given names
     */
    Map<UUID, String> getWhitelistedPlayers();

    /**
     * Removes a player from the maintenance whitelist.
     *
     * @param uuid uuid of the player to remove
     * @return true if removing the player was successful
     */
    boolean removeWhitelistedPlayer(UUID uuid);

    /**
     * Removes a player from the maintenance whitelist.
     *
     * @param name name of the player to remove with original lower-/uppercase letters
     * @return true if removing the player was successful
     * @deprecated if a player changes their name, the list will still contain the old one
     * @see #removeWhitelistedPlayer(UUID)
     */
    @Deprecated
    boolean removeWhitelistedPlayer(String name);

    /**
     * Adds a player to the maintenance whitelist.
     *
     * @param uuid uuid of the player to add
     * @param name of the player to add
     * @return true if adding the player was successful
     */
    boolean addWhitelistedPlayer(UUID uuid, String name);

    /**
     * Reloads maintenance-icon, config, language file and WhitelistedPlayers.
     */
    void reloadConfigs();

    /**
     * This indicates if the optional 'debug' value in the config is set to true.
     *
     * @return true if debug is enabled
     */
    boolean debugEnabled();
}
