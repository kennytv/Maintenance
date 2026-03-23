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
package eu.kennytv.maintenance.api;

import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface Settings {

    /**
     * Returns whether maintenance is currently enabled.
     *
     * @return true if maintenance is currently enabled
     */
    boolean isMaintenance();

    /**
     * Returns the current active maintenance mode, or null is set to default.
     * Used when getting MOTD/player count messages.
     *
     * @return the current active maintenance mode, or null if set to default
     */
    @Nullable String activeMode();

    /**
     * Returns the reason for the current active mode.
     *
     * @return variable reason for the current active mode, or empty if no reason is set
     */
    String activeReason();

    /**
     * Sets the reason for the current active mode, used in %REASON% message placeholders.
     *
     * @param reason reason to set for the current active mode, or null to remove the reason
     */
    void setActiveReason(@Nullable String reason);

    /**
     * @return true if custom ping messages during maintenance should be used
     */
    boolean isEnablePingMessages();

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
     * Returns whether the uuid is whitelisted.
     *
     * @param uuid uuid of the player to check
     * @return true if the uuid is whitelisted
     */
    boolean isWhitelisted(UUID uuid);

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
     * @see #removeWhitelistedPlayer(UUID)
     * @deprecated if a player changes their name, the list will still contain the old one
     */
    @Deprecated
    boolean removeWhitelistedPlayer(String name);

    /**
     * Adds a player to the maintenance whitelist.
     *
     * @param uuid uuid of the player to add
     * @param name of the player to add
     * @return true if adding the player was successful, false if already on the whitelist
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
