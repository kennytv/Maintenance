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
package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.event.MaintenanceReloadedEvent;
import eu.kennytv.maintenance.core.config.Config;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.TextComponent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.minimessage.MiniMessage;
import eu.kennytv.maintenance.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public class Settings implements eu.kennytv.maintenance.api.Settings {
    public static final String NEW_LINE_REPLACEMENT = "<br>";
    private static final int CONFIG_VERSION = 9;
    private static final int LANGUAGE_VERSION = 2;
    private static final Random RANDOM = new Random();
    protected final MaintenancePlugin plugin;
    private final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final String[] unsupportedFields;
    protected boolean maintenance;
    private Set<Integer> broadcastIntervals;
    private List<String> pingMessages;
    private List<String> timerSpecificPingMessages;
    private List<String> commandsOnMaintenanceEnable;
    private List<String> commandsOnMaintenanceDisable;
    private String legacyParsedPlayerCountMessage;
    private String legacyParsedTimerPlayerCountMessage;
    private List<String> legacyParsedPlayerCountHoverLines;
    private List<String> legacyParsedTimerPlayerCountHoverLines;
    private String prefixString;
    private String languageName;
    private boolean enablePingMessages;
    private boolean customPlayerCountMessage;
    private boolean customPlayerCountHoverMessage;
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
        } catch (final Exception e) {
            throw new RuntimeException("Unable to load Maintenance files - probably a malformed config file", e);
        }

        loadSettings();
        createLanguageFile();

        try {
            language = new Config(new File(plugin.getDataFolder(), "language-" + languageName + ".yml"));
            language.load();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance language file - probably a malformed file", e);
        }

        try {
            updateLanguageFile();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't update language file", e);
        }

        prefixString = language.getString("prefix");

        plugin.getEventManager().callEvent(new MaintenanceReloadedEvent());
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't save the config file!", e);
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
            plugin.getLogger().log(Level.SEVERE, "Couldn't save the whitelisted players file!", e);
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

        final ConfigSection pingMessageSection = config.getSection("ping-message");
        enablePingMessages = pingMessageSection.getBoolean("enabled", true);
        // Can't store this pre-parsed as components because gradients will break replacements
        pingMessages = loadPingMessages(pingMessageSection.getStringList("messages"));
        if (pingMessageSection.getBoolean("enable-timer-specific-messages")) {
            timerSpecificPingMessages = loadPingMessages(pingMessageSection.getStringList("timer-messages"));
        } else {
            timerSpecificPingMessages = null;
        }

        maintenance = config.getBoolean("maintenance-enabled");
        commandsOnMaintenanceEnable = config.getStringList("commands-on-maintenance-enable");
        commandsOnMaintenanceDisable = config.getStringList("commands-on-maintenance-disable");
        customMaintenanceIcon = config.getBoolean("custom-maintenance-icon");
        joinNotifications = config.getBoolean("send-join-notification");
        broadcastIntervals = new HashSet<>(config.getIntList("timer-broadcast-for-seconds"));

        // Player count and player list hover messages are only accepted and rendered as legacy chat
        if (plugin.getServerType() != ServerType.SPONGE) {
            final ConfigSection playerCountMessageSection = config.getSection("player-count-message");
            customPlayerCountMessage = playerCountMessageSection.getBoolean("enabled");
            legacyParsedPlayerCountMessage = toLegacy(parse(getConfigMessage(playerCountMessageSection, "message")));
            if (playerCountMessageSection.getBoolean("enable-timer-specific-message")) {
                legacyParsedTimerPlayerCountMessage = toLegacy(parse(getConfigMessage(playerCountMessageSection, "timer-message")));
            } else {
                legacyParsedTimerPlayerCountMessage = null;
            }
        }

        final ConfigSection listHoverMessageSection = config.getSection("player-list-hover-message");
        customPlayerCountHoverMessage = listHoverMessageSection.getBoolean("enabled");
        legacyParsedPlayerCountHoverLines = new ArrayList<>();
        for (final String line : listHoverMessageSection.getString("message").split("<br>")) {
            legacyParsedPlayerCountHoverLines.add(toLegacy(parse(line)));
        }
        if (listHoverMessageSection.getBoolean("enable-timer-specific-message")) {
            legacyParsedTimerPlayerCountHoverLines = new ArrayList<>();
            for (final String line : listHoverMessageSection.getString("timer-message").split("<br>")) {
                legacyParsedTimerPlayerCountHoverLines.add(toLegacy(parse(line)));
            }
        } else {
            legacyParsedTimerPlayerCountHoverLines = null;
        }

        languageName = config.getString("language").toLowerCase();
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
                whitelistedPlayers.put(UUID.fromString(entry.getKey()), String.valueOf(entry.getValue()));
            } catch (final IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid WhitelistedPlayers entry: " + entry.getKey());
            }
        }

        loadExtraSettings();
    }

    private void updatePluginDirectory() {
        // All plugin identifiers were changed to 'Maintenance' ('maintenance' for Sponge and Velocity) in 3.0.5
        String oldDirName = "Maintenance" + plugin.getServerType();
        if (plugin.getServerType() == ServerType.SPONGE || plugin.getServerType() == ServerType.VELOCITY) {
            oldDirName = oldDirName.toLowerCase(Locale.ROOT);
        } else if (plugin.getServerType() == ServerType.SPIGOT) {
            oldDirName = "MaintenanceSpigot";
        }

        final File oldDir = new File(plugin.getDataFolder().getParentFile(), oldDirName);
        if (!oldDir.exists()) {
            return;
        }

        try {
            Files.move(oldDir.toPath(), plugin.getDataFolder().toPath());
            plugin.getLogger().info("Moved old " + oldDirName + " to new " + plugin.getDataFolder().getName() + " directory!");
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while copying " + oldDirName + " to new " + plugin.getDataFolder().getName() + " directory!", e);
        }
    }

    private void updateConfig() {
        final int version = config.getInt("config-version");
        if (version != CONFIG_VERSION) {
            plugin.getLogger().info("Updating config to the latest version...");
            if (version < 6) {
                // Legacy text to MiniMessage
                config.getStringList("pingmessages").replaceAll(this::legacyToMinimessage);
                config.getStringList("timerspecific-pingmessages").replaceAll(this::legacyToMinimessage);
                config.set("playercounthovermessage", legacyToMinimessage(config.getString("playercounthovermessage")));
                config.set("playercountmessage", legacyToMinimessage(config.getString("playercountmessage")));
            }
            if (version < 9) {
                config.move("enable-pingmessages", "ping-message.enabled");
                config.move("pingmessages", "ping-message.messages");
                config.move("enable-timerspecific-messages", "ping-message.enable-timer-specific-messages");
                config.move("timerspecific-pingmessages", "ping-message.timer-messages");

                config.move("enable-playercountmessage", "player-count-message.enabled");
                config.move("playercountmessage", "player-count-message.message");

                config.move("enable-playercounthovermessage", "player-list-hover-message.enabled");
                config.move("playercounthovermessage", "player-list-hover-message.message");
            }

            createFile("config-new.yml", "config.yml");
            final File file = new File(plugin.getDataFolder(), "config-new.yml");
            final Config tempConfig = new Config(file, unsupportedFields);
            try {
                tempConfig.load();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            config.addMissingFields(tempConfig);
            config.replaceComments(tempConfig);
            config.set("config-version", CONFIG_VERSION);

            file.delete();
            tempConfig.clear();

            saveConfig();
            plugin.getLogger().info("Done! Updated config!");
        }
    }

    private void updateLanguageFile() throws IOException {
        final int version = language.getInt("language-version");
        if (version == LANGUAGE_VERSION) {
            return;
        }

        plugin.getLogger().info("Updating language file to the latest version...");
        if (version < 1) {
            for (final Map.Entry<String, Object> entry : language.getValues().entrySet()) {
                if (!(entry.getValue() instanceof String)) {
                    continue;
                }

                String value = (String) entry.getValue();
                value = value.replace("&8[&eMaintenance&8] ", "<prefix>");
                value = legacyToMinimessage(value);
                value = value.replace("%NEWLINE%", NEW_LINE_REPLACEMENT);
                language.set(entry.getKey(), value);
            }
        }

        final String filePrefix = "language-" + languageName;
        createFile(filePrefix + "-new.yml", filePrefix + ".yml");

        final File file = new File(plugin.getDataFolder(), filePrefix + "-new.yml");
        final Config tempConfig = new Config(file);
        try {
            tempConfig.load();
        } finally {
            file.delete();
        }

        language.addMissingFields(tempConfig);
        language.replaceComments(tempConfig);
        tempConfig.clear();

        language.set("language-version", LANGUAGE_VERSION);

        language.save();
        plugin.getLogger().info("Updated language file!");
    }

    private String legacyToMinimessage(final String s) {
        final TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(s);
        final String serialized = MiniMessage.miniMessage().serialize(component);
        //TODO hack to remove explicit closing until fixed in MM
        return serialized.replaceAll("</[a-z_]+>", "");
    }

    private String getConfigMessage(final ConfigSection section, final String path) {
        final String s = section.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The config file is missing the following string: " + path);
            return path;
        }
        return replaceNewlineVar(s);
    }

    public String getConfigMessage(final String path) {
        return getConfigMessage(config, path);
    }

    public String getLanguageString(final String path, final String... replacements) {
        String s = language.getString(path);
        if (s == null) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return path;
        }
        if (replacements.length != 0) {
            if (replacements.length % 2 != 0) {
                throw new IllegalArgumentException("Invalid replacement count: " + replacements.length);
            }

            for (int i = 0; i < replacements.length; i += 2) {
                final String key = replacements[i];
                s = s.replace(key, replacements[i + 1]);
            }
        }
        return replaceNewlineVar(s).replace("<prefix>", prefixString);
    }

    public @Nullable String getLanguageStringOrNull(final String path, final String... replacements) {
        return language.contains(path) ? getLanguageString(path, replacements) : null;
    }

    public Component getMessage(final String path, final String... replacements) {
        return parse(getLanguageString(path, replacements));
    }

    // TODO Cache platform components if there are no replacements
    public Component getRandomPingMessage() {
        if (plugin.isTaskRunning() && !plugin.getRunnable().shouldEnable()
                && hasTimerSpecificPingMessages() && !timerSpecificPingMessages.isEmpty()) {
            return getPingMessage(timerSpecificPingMessages);
        }
        return pingMessages.isEmpty() ? Component.empty() : getPingMessage(pingMessages);
    }

    private Component getPingMessage(final List<String> list) {
        final String component = list.size() == 1 ? list.get(0) : list.get(RANDOM.nextInt(list.size()));
        return parse(plugin.replacePingVariables(component));
    }

    private List<String> loadPingMessages(final List<String> list) {
        final List<String> components = new ArrayList<>(list.size());
        for (final String s : list) {
            components.add(replaceNewlineVar(s));
        }
        return components;
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (whitelistedPlayers.remove(uuid) == null) {
            return false;
        }

        whitelist.remove(uuid.toString());
        saveWhitelistedPlayers();
        return true;
    }

    @Override
    public boolean removeWhitelistedPlayer(final String name) {
        UUID uuid = null;
        for (final Map.Entry<UUID, String> e : whitelistedPlayers.entrySet()) {
            if (e.getValue().equalsIgnoreCase(name)) {
                uuid = e.getKey();
                break;
            }
        }

        if (uuid == null) {
            return false;
        }

        whitelistedPlayers.remove(uuid);
        whitelist.remove(uuid.toString());
        saveWhitelistedPlayers();
        return true;
    }

    @Override
    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        final boolean added = whitelistedPlayers.put(uuid, name) == null;
        whitelist.set(uuid.toString(), name);
        saveWhitelistedPlayers();
        return added;
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
    public boolean isEnablePingMessages() {
        return enablePingMessages;
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
        if (savedEndtimer == millis) {
            return;
        }

        this.savedEndtimer = millis;
        config.getSection("continue-endtimer-after-restart").set("end", millis);
        saveConfig();
    }

    public Config getConfig() {
        return config;
    }

    public List<String> getPingMessages() {
        return pingMessages;
    }

    public List<String> getCommandsOnMaintenanceEnable() {
        return commandsOnMaintenanceEnable;
    }

    public List<String> getCommandsOnMaintenanceDisable() {
        return commandsOnMaintenanceDisable;
    }

    public @Nullable List<String> getTimerSpecificPingMessages() {
        return timerSpecificPingMessages;
    }

    public Set<Integer> getBroadcastIntervals() {
        return broadcastIntervals;
    }

    public String getLegacyParsedPlayerCountMessage() {
        final String message = timerDependent(legacyParsedPlayerCountMessage, legacyParsedTimerPlayerCountMessage);
        return plugin.replacePingVariables(message);
    }

    public String[] getLegacyParsedPlayerCountHoverLines() {
        final List<String> lines = timerDependent(legacyParsedPlayerCountHoverLines, legacyParsedTimerPlayerCountHoverLines);
        final String[] parsedLines = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            parsedLines[i] = plugin.replacePingVariables(lines.get(i));
        }
        return parsedLines;
    }

    private <T> T timerDependent(final T normal, final @Nullable T timerSpecific) {
        return timerSpecific != null && plugin.isTaskRunning() && !plugin.getRunnable().shouldEnable() ? timerSpecific : normal;
    }

    public Component getKickMessage() {
        return parse(plugin.replacePingVariables(getLanguageString("kickmessage")));
    }

    public String getLanguage() {
        return languageName;
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    public boolean hasCustomPlayerCountHoverMessage() {
        return customPlayerCountHoverMessage;
    }

    protected Component parse(final String s) {
        return MiniMessage.miniMessage().deserialize(s);
    }

    protected String toLegacy(final Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /*
     * Note on why this even exists: Yaml will force save all strings containing line breaks '\n' in this rather chunky format:
     *   key: |-
     *     First line text
     *      Second line text.
     *      ...
     *
     * This also happens to string lists, making those practically unreadable (in particular the motd list), as well as confusing for most users in general.
     * Because of this, I replace <br> manually, to spare users from these ugly breaks (before I inevitably switch to a different configuration library).
     */
    protected String replaceNewlineVar(final String s) {
        return s.replace(NEW_LINE_REPLACEMENT, "\n");
    }

    protected void loadExtraSettings() {
    }
}
