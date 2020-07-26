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

package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.core.config.Config;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.ServerType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Settings implements ISettings {
    private static final int CURRENT_CONFIG_VERSION = 4;
    private static final Random RANDOM = new Random();
    protected final MaintenancePlugin plugin;
    private final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final String[] unsupportedFields;
    protected boolean maintenance;
    private Set<Integer> broadcastIntervals;
    private List<String> pingMessages;
    private List<String> timerSpecificPingMessages;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private String languageName;
    private boolean customPlayerCountMessage;
    private boolean customMaintenanceIcon;
    private boolean joinNotifications;
    private boolean updateChecks;
    private boolean saveEndtimerOnStop;
    private boolean kickOnlinePlayers;
    private boolean debug;
    private long savedEndtimer;

    protected Config config;
    protected Config language;
    protected Config whitelist;

    public Settings(final MaintenancePlugin plugin, final String... unsupportedFields) {
        this.plugin = plugin;
        this.unsupportedFields = unsupportedFields;
        if (!plugin.getDataFolder().exists()) {
            updatePluginDirectory();
            plugin.getDataFolder().mkdirs();
        }

        createFile("config.yml");
        createFile("WhitelistedPlayers.yml");

        reloadConfigs();
    }

    @Override
    public void reloadConfigs() {
        try {
            config = new Config(new File(plugin.getDataFolder(), "config.yml"), unsupportedFields);
            config.load();
            config.resetAwesomeHeader();
            whitelist = new Config(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
            whitelist.load();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance files!", e);
        }

        loadSettings();
        createLanguageFile();

        try {
            language = new Config(new File(plugin.getDataFolder(), "language-" + languageName + ".yml"));
            language.load();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance language file!", e);
        }

        updateLanguageFile();

        // Directly cache colored messages - this should not be saved!
        transformColoredMessages(language.getValues());
    }

    private void transformColoredMessages(final Map<String, Object> map) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                transformColoredMessages((Map<String, Object>) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                entry.setValue(getColoredString(replaceNewlineVar((String) entry.getValue())));
            }
        }
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    // Public, as it is used in the MaintenanceAddon
    public void createFile(final String name) {
        createFile(name, name);
    }

    private void createFile(final String name, final String from) {
        final File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try (final InputStream in = plugin.getResource(from)) {
                Files.copy(in, file.toPath());
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create " + name + " file for Maintenance!", e);
            }
        }
    }

    private void saveWhitelistedPlayers() {
        try {
            whitelist.save();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void createLanguageFile() {
        final String fileName = "language-" + languageName + ".yml";
        final File file = new File(plugin.getDataFolder(), fileName);
        if (file.exists()) return;

        try (final InputStream in = plugin.getResource(fileName)) {
            Files.copy(in, file.toPath());
        } catch (final IOException e) {
            plugin.getLogger().warning("Unable to provide language " + languageName);
            // Fall back to English language (if not already selected)
            if (!languageName.equals("en")) {
                plugin.getLogger().warning("Falling back to default language: en");
                languageName = "en";
                createLanguageFile();
            }
        }
    }

    private void loadSettings() {
        updateConfig();

        pingMessages = config.getStringList("pingmessages");
        if (config.getBoolean("enable-timerspecific-messages")) {
            timerSpecificPingMessages = config.getStringList("timerspecific-pingmessages");
        }
        maintenance = config.getBoolean("maintenance-enabled");
        customPlayerCountMessage = config.getBoolean("enable-playercountmessage");
        customMaintenanceIcon = config.getBoolean("custom-maintenance-icon");
        joinNotifications = config.getBoolean("send-join-notification");
        broadcastIntervals = new HashSet<>(config.getIntList("timer-broadcast-for-seconds"));
        if (plugin.getServerType() != ServerType.SPONGE) {
            playerCountMessage = getColoredString(getConfigString("playercountmessage"));
        }
        playerCountHoverMessage = replaceNewlineVar(getColoredString(getConfigString("playercounthovermessage")));
        languageName = getConfigString("language").toLowerCase();
        kickOnlinePlayers = config.getBoolean("kick-online-players", true);
        updateChecks = config.getBoolean("update-checks", true);
        debug = config.getBoolean("debug");

        final ConfigSection section = config.getSection("continue-endtimer-after-restart");
        saveEndtimerOnStop = section.getBoolean("enabled");
        savedEndtimer = section.getLong("end");

        if (customMaintenanceIcon) {
            plugin.loadMaintenanceIcon();
        }

        whitelistedPlayers.clear();
        for (final Map.Entry<String, Object> entry : whitelist.getValues().entrySet()) {
            try {
                whitelistedPlayers.put(UUID.fromString(entry.getKey()), (String) entry.getValue());
            } catch (final Exception e) {
                plugin.getLogger().warning("invalid WhitelistedPlayers entry: " + entry.getKey());
            }
        }

        loadExtraSettings();
    }

    private void updatePluginDirectory() {
        // All plugin identifiers were changed to 'Maintenance' ('maintenance' for Sponge and Velocity) in 3.0.5
        // Don't worry, this is only checked if no plugin folder is found
        String oldDirName = "Maintenance" + plugin.getServerType();
        if (plugin.getServerType() == ServerType.SPONGE || plugin.getServerType() == ServerType.VELOCITY) {
            oldDirName = oldDirName.toLowerCase();
        }

        final File oldDir = new File(plugin.getDataFolder().getParentFile(), oldDirName);
        if (!oldDir.exists()) return;

        try {
            Files.move(oldDir.toPath(), plugin.getDataFolder().toPath());
            plugin.getLogger().info("Moved old " + oldDirName + " to new " + plugin.getDataFolder().getName() + " directory!");
        } catch (final IOException e) {
            plugin.getLogger().severe("Error while copying " + oldDirName + " to new " + plugin.getDataFolder().getName() + " directory!");
            e.printStackTrace();
        }
    }

    private void updateConfig() {
        boolean changed = false;

        // Update config to latest version (config version included since 3.0.1)
        if (config.getInt("config-version") != CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("Updating config to latest version...");
            createFile("config-new.yml", "config.yml");
            final File file = new File(plugin.getDataFolder(), "config-new.yml");
            final Config tempConfig = new Config(file, unsupportedFields);
            try {
                tempConfig.load();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            config.addMissingFields(tempConfig.getValues(), tempConfig.getComments());
            config.set("config-version", CURRENT_CONFIG_VERSION);

            file.delete();
            tempConfig.clear();

            changed = true;
        }

        if (changed) {
            saveConfig();
            plugin.getLogger().info("Done! Updated config!");
        }
    }

    private void updateLanguageFile() {
        final String filePrefix = "language-" + languageName;
        try {
            createFile(filePrefix + "-new.yml", filePrefix + ".yml");
        } catch (final NullPointerException e) {
            plugin.getLogger().info("Not checking for updated language strings, since there is no " + filePrefix + ".yml in the resource files (if your file is self translated and up to date, you can ignore this).");
            return;
        } catch (final Exception e) {
            plugin.getLogger().warning("Couldn't update language file, as the " + filePrefix + ".yml could not be loaded from the resource files!");
            e.printStackTrace();
            return;
        }

        final File file = new File(plugin.getDataFolder(), filePrefix + "-new.yml");
        final Config tempConfig = new Config(file);
        try {
            tempConfig.load();
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        }

        final boolean updated = language.addMissingFields(tempConfig.getValues(), tempConfig.getComments());
        tempConfig.clear();
        file.delete();
        if (updated) {
            try {
                language.save();
                plugin.getLogger().info("Updated language file!");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

    public String getColoredString(final String s) {
        // Method taken from Bungee
        final char[] b = s.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && ALL_CODES.indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public String getConfigString(final String path) {
        final String s = config.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The config is missing the following string: " + path);
            return "null";
        }
        return s;
    }

    public String getMessage(final String path) {
        return getMessage(path, "null");
    }

    public String getMessage(final String path, final String def) {
        final String s = language.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return def;
        }
        return s;
    }

    public String getRandomPingMessage() {
        if (plugin.isTaskRunning() && !plugin.getRunnable().shouldEnable()
                && hasTimerSpecificPingMessages() && !timerSpecificPingMessages.isEmpty()) {
            return getPingMessage(timerSpecificPingMessages);
        }
        return pingMessages.isEmpty() ? "" : getPingMessage(pingMessages);
    }

    private String getPingMessage(final List<String> list) {
        final String s = list.size() == 1 ? list.get(0) : list.get(RANDOM.nextInt(list.size()));
        return getColoredString(replaceNewlineVar(plugin.formatedTimer(s)));
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (!whitelistedPlayers.containsKey(uuid)) return false;
        whitelistedPlayers.remove(uuid);
        whitelist.remove(uuid.toString());
        saveWhitelistedPlayers();
        return true;
    }

    @Deprecated
    @Override
    public boolean removeWhitelistedPlayer(final String name) {
        UUID uuid = null;
        for (final Map.Entry<UUID, String> e : whitelistedPlayers.entrySet()) {
            if (e.getValue().equalsIgnoreCase(name)) {
                uuid = e.getKey();
                break;
            }
        }

        if (uuid == null) return false;

        whitelistedPlayers.remove(uuid);
        whitelist.remove(uuid.toString());
        saveWhitelistedPlayers();
        return true;
    }

    @Override
    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        final boolean contains = !whitelistedPlayers.containsKey(uuid);
        whitelistedPlayers.put(uuid, name);
        whitelist.set(uuid.toString(), name);
        saveWhitelistedPlayers();
        return contains;
    }

    @Override
    public Map<UUID, String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    @Override
    public boolean isWhitelisted(final UUID uuid) {
        return whitelistedPlayers.containsKey(uuid);
    }

    @Override
    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(final boolean maintenance) {
        this.maintenance = maintenance;
    }

    @Override
    public boolean isJoinNotifications() {
        return joinNotifications;
    }

    @Override
    public boolean hasCustomIcon() {
        return customMaintenanceIcon;
    }

    @Override
    public boolean debugEnabled() {
        return debug;
    }

    public boolean hasUpdateChecks() {
        return updateChecks;
    }

    public boolean isSaveEndtimerOnStop() {
        return saveEndtimerOnStop;
    }

    public boolean hasTimerSpecificPingMessages() {
        return timerSpecificPingMessages != null;
    }

    public boolean isKickOnlinePlayers() {
        return kickOnlinePlayers;
    }

    public long getSavedEndtimer() {
        return savedEndtimer;
    }

    public void setSavedEndtimer(final long millis) {
        if (savedEndtimer == millis) return;
        this.savedEndtimer = millis;
        config.getSection("continue-endtimer-after-restart").set("end", millis);
        saveConfig();
    }

    public Config getConfig() {
        return config;
    }

    // The ping messages still contain the %NEWLINE% (if they have 2 lines)
    public List<String> getPingMessages() {
        return pingMessages;
    }

    @Nullable
    public List<String> getTimerSpecificPingMessages() {
        return timerSpecificPingMessages;
    }

    public Set<Integer> getBroadcastIntervals() {
        return broadcastIntervals;
    }

    public String getPlayerCountMessage() {
        return plugin.formatedTimer(playerCountMessage);
    }

    public String getPlayerCountHoverMessage() {
        return plugin.formatedTimer(playerCountHoverMessage);
    }

    public String getKickMessage() {
        return plugin.formatedTimer(getMessage("kickmessage"));
    }

    public String getLanguage() {
        return languageName;
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    /*
     * Note on why this even exists: Yaml will force save all strings containing line breaks '\n' in this rather chunky format:
     *   key: |-
     *     First line text
     *      Second line text.
     *      ...
     *
     * This also happens to string lists, making those practically unreadable (in partciular the motd list), as well as confusing for most users in general.
     * Because of this, I replace %NEWLINE% manually, to spare users from these ugly breaks.
     */
    protected String replaceNewlineVar(final String s) {
        return s.replace("%NEWLINE%", "\n");
    }

    protected void loadExtraSettings() {
    }
}
