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

package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.core.config.Config;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.ServerType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Settings implements ISettings {
    private static final int CURRENT_CONFIG_VERSION = 3;
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
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

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

        // Directly save colored messages
        for (final Map.Entry<String, Object> entry : language.getValues().entrySet()) {
            if (!(entry.getValue() instanceof String)) continue;
            entry.setValue(getColoredString((String) entry.getValue()));
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
                throw new IllegalArgumentException("Unable to create " + name + " file for Maintenance!", e);
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
        final File file = new File(plugin.getDataFolder(), "language-" + languageName + ".yml");
        if (!file.exists()) {
            try (final InputStream in = plugin.getResource("language-" + languageName + ".yml")) {
                Files.copy(in, file.toPath());
            } catch (final IOException e) {
                plugin.getLogger().warning("Unable to provide language " + languageName);
                if (!languageName.equals("en")) {
                    plugin.getLogger().warning("Falling back to default language: en");
                    languageName = "en";
                    createLanguageFile();
                }
            }
        }
    }

    private void loadSettings() {
        updateConfig();

        pingMessages = config.getStringList("pingmessages");
        if (config.getBoolean("enable-timerspecific-messages"))
            timerSpecificPingMessages = config.getStringList("timerspecific-pingmessages");
        maintenance = config.getBoolean("maintenance-enabled");
        customPlayerCountMessage = config.getBoolean("enable-playercountmessage");
        customMaintenanceIcon = config.getBoolean("custom-maintenance-icon");
        joinNotifications = config.getBoolean("send-join-notification");
        broadcastIntervals = new HashSet<>(config.getIntList("timer-broadcast-for-seconds"));
        if (plugin.getServerType() != ServerType.SPONGE)
            playerCountMessage = getColoredString(getConfigString("playercountmessage"));
        playerCountHoverMessage = getColoredString(getConfigString("playercounthovermessage"));
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
        whitelist.getValues().forEach((key, value) -> {
            try {
                whitelistedPlayers.put(UUID.fromString(key), (String) value);
            } catch (final Exception e) {
                plugin.getLogger().warning("invalid WhitelistedPlayers entry: " + key);
            }
        });
        loadExtraSettings();
    }

    private void updateConfig() {
        boolean changed = false;

        // 3.0 - update config format from 2.5
        if (migrateConfig(new File(plugin.getDataFolder(), "bungee-config.yml"))
                || migrateConfig(new File(plugin.getDataFolder(), "spigot-config.yml"))) {
            // Also rename old language file
            final File file = new File(plugin.getDataFolder(), "language-" + languageName + ".yml");
            if (file.exists()) {
                if (file.renameTo(new File(plugin.getDataFolder(), "language-" + languageName + ".old")))
                    plugin.getLogger().info("Renamed old language file!");
                else
                    plugin.getLogger().warning("Could not rename old language file! Please rename/delete it yourself as soon as possible!");
            }

            changed = true;
        }
        // 3.0 - move SpigotServer.yml fields to config
        if (plugin.getServerType() == ServerType.BUNGEE && migrateSpigotServersFile()) {
            changed = true;
        }
        // 3.0 - move maintenace-icon from server to plugin directory
        final File icon = new File("maintenance-icon.png");
        if (icon.exists()) {
            if (icon.renameTo(new File(plugin.getDataFolder(), "maintenance-icon.png")))
                plugin.getLogger().info("Moved maintenance-icon from server directory to the plugin's directory!");
            else
                plugin.getLogger().warning("Could not move maintenance-icon from server directory to the plugin's directory! Please do so yourself!");
        }

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
        final String s = "language-" + languageName;
        try {
            createFile(s + "-new.yml", s + ".yml");
        } catch (final IllegalArgumentException e) {
            plugin.getLogger().warning("Couldn't update language file, as the " + s + ".yml wasn't found in the resource files!");
            e.printStackTrace();
            return;
        }

        final File file = new File(plugin.getDataFolder(), s + "-new.yml");
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
            } catch (final IOException e) {
                e.printStackTrace();
                return;
            }
            plugin.getLogger().info("Updated language file!");
        }
    }

    private boolean migrateConfig(final File file) {
        if (!file.exists()) return false;

        plugin.getLogger().info("Migrating old config to new format...");
        final Config oldConfig = new Config(file);
        try {
            oldConfig.load();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while trying to migrate old config file");
            e.printStackTrace();
            return false;
        }

        if (oldConfig.contains("pingmessage"))
            config.set("pingmessages", Arrays.asList(oldConfig.getString("pingmessage")));
        if (oldConfig.contains("enable-maintenance-mode"))
            config.set("maintenance-enabled", oldConfig.getBoolean("enable-maintenance-mode"));
        config.getValues().entrySet().forEach(entry -> {
            if (!oldConfig.contains(entry.getKey())) return;
            entry.setValue(oldConfig.get(entry.getKey()));
        });

        oldConfig.clear();
        if (!file.delete())
            plugin.getLogger().warning("Could not delete old config file! Please delete it as soon as possible.");
        else
            plugin.getLogger().info("Updated to new config file!");
        return true;
    }

    private boolean migrateSpigotServersFile() {
        final File file = new File(plugin.getDataFolder(), "SpigotServers.yml");
        if (!file.exists()) return false;

        final Config oldFile = new Config(file);
        try {
            oldFile.load();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while trying to migrate old SpigotServers file");
            e.printStackTrace();
            return false;
        }

        if (oldFile.contains("maintenance-on"))
            config.set("proxied-maintenance-servers", oldFile.getStringList("maintenance-on"));
        if (oldFile.contains("fallback"))
            config.set("fallback", oldFile.getString("fallback"));

        oldFile.clear();
        if (!file.delete())
            plugin.getLogger().warning("Could not delete old SpigotServers.yml file! Please delete it as soon as possible.");
        else
            plugin.getLogger().info("Deleted old SpigotServers.yml file!");
        return true;
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
        if (!config.contains(path)) {
            plugin.getLogger().warning("The config is missing the following string: " + path);
            return "null";
        }
        return config.getString(path);
    }

    public String getMessage(final String path) {
        return getMessage(path, "null");
    }

    public String getMessage(final String path, final String def) {
        if (!language.contains(path)) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return def;
        }
        return language.getString(path);
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
        return getColoredString(plugin.formatedTimer(s).replace("%NEWLINE%", "\n"));
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
        final Map.Entry<UUID, String> entry = whitelistedPlayers.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(name)).findAny().orElse(null);
        if (entry == null) return false;

        final UUID uuid = entry.getKey();
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

    public List<String> getPingMessages() {
        return pingMessages;
    }

    /**
     * May be null
     */
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
        return plugin.formatedTimer(getMessage("kickmessage", "§cThe server is currently under maintenance!%NEWLINE%§cTry again later!").replace("%NEWLINE%", "\n"));
    }

    public String getLanguage() {
        return languageName;
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    protected void loadExtraSettings() {
    }
}
