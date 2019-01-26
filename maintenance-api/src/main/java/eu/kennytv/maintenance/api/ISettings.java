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
     * Reloads the maintenance-icon.png image from the server folder.
     *
     * @return true if loading the image was successful
     */
    boolean reloadMaintenanceIcon();

    /**
     * This indicates if the optional 'debug' value in the config is set to true.
     *
     * @return true if debug is enabled
     */
    boolean debugEnabled();
}
