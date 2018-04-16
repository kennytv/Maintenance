package eu.kennytv.maintenance.api;

import java.util.Map;
import java.util.UUID;

public interface ISettings {

    /**
     * @return true if maintenance is enabled on the proxy
     */
    boolean isMaintenance();

    /**
     * @return true if "join-notification" is set to true in the config
     */
    boolean isJoinNotifications();

    /**
     * @return true if "custom-maintenance-icon" is set to true in the config
     */
    boolean hasCustomIcon();

    /**
     * The keyset of the map contains the uuids as a string, the values the names of the players.
     *
     * @return map of uuids of whitelisted players with their last seen names
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
     * @param name name of the player to remove in lowercase letters
     * @return true if removing the player was successful
     * @deprecated if a player changes their name, the list will still contain their old one
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
}
