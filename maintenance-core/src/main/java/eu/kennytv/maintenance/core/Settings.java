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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Settings implements ISettings {
    private static final Random RANDOM = new Random();
    protected final MaintenanceModePlugin plugin;
    private final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final String[] unsupportedFields;
    protected boolean maintenance;
    private Set<Integer> broadcastIntervalls;
    private List<String> pingMessages;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private String languageName;
    private boolean customPlayerCountMessage;
    private boolean customMaintenanceIcon;
    private boolean joinNotifications;
    private boolean debug;

    protected Config config;
    protected Config language;
    protected Config whitelist;

    public Settings(final MaintenanceModePlugin plugin, final String... unsupportedFields) {
        this.plugin = plugin;
        this.unsupportedFields = unsupportedFields;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        createFile("config.yml");
        createFile("WhitelistedPlayers.yml");
        createExtraFiles();

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
            reloadExtraConfigs();
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

    protected void createFile(final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try (final InputStream in = plugin.getResource(name)) {
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
        maintenance = config.getBoolean("maintenance-enabled");
        customPlayerCountMessage = config.getBoolean("enable-playercountmessage");
        customMaintenanceIcon = config.getBoolean("custom-maintenance-icon");
        joinNotifications = config.getBoolean("send-join-notification");
        broadcastIntervalls = new HashSet<>(config.getIntList("timer-broadcast-for-seconds"));
        playerCountMessage = getColoredString(getConfigString("playercountmessage"));
        playerCountHoverMessage = getColoredString(getConfigString("playercounthovermessage"));
        languageName = getConfigString("language").toLowerCase();
        debug = config.getBoolean("debug");
        if (customMaintenanceIcon) {
            plugin.loadMaintenanceIcon();
        }

        whitelistedPlayers.clear();
        whitelist.getKeys().forEach(key -> whitelistedPlayers.put(UUID.fromString(key), whitelist.getString(key)));
        loadExtraSettings();
    }

    private void updateConfig() {
        boolean changed = false;

        // 3.0 - update config format from 2.5
        if (migrateConfig(new File(plugin.getDataFolder(), "bungee-config.yml"))
                || migrateConfig(new File(plugin.getDataFolder(), "spigot-config.yml"))) {
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

        // ...

        if (changed) {
            plugin.getLogger().info("Updated config to the latest version!");
            saveConfig();
        }
    }

    private boolean migrateConfig(final File file) {
        if (!file.exists()) return false;

        plugin.getLogger().info("Migrating old config to new format...");
        final Config oldConfig = new Config(file);
        try {
            oldConfig.load();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while trying to migrate old config file", e);
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
        if (pingMessages.isEmpty()) return "";
        final String s = pingMessages.size() > 1 ? pingMessages.get(RANDOM.nextInt(pingMessages.size())) : pingMessages.get(0);
        return getColoredString(s.replace("%NEWLINE%", "\n").replace("%TIMER%", plugin.formatedTimer()));
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (!whitelistedPlayers.containsKey(uuid)) return false;
        whitelistedPlayers.remove(uuid);
        whitelist.set(uuid.toString(), null);
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
        whitelist.set(uuid.toString(), null);
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

    public Config getConfig() {
        return config;
    }

    public List<String> getPingMessages() {
        return pingMessages;
    }

    public Set<Integer> getBroadcastIntervalls() {
        return broadcastIntervalls;
    }

    public String getPlayerCountMessage() {
        return playerCountMessage.replace("%TIMER%", plugin.formatedTimer());
    }

    public String getPlayerCountHoverMessage() {
        return playerCountHoverMessage.replace("%TIMER%", plugin.formatedTimer());
    }

    public String getKickMessage() {
        return getMessage("kickmessage", "&cThe server is currently under maintenance!%NEWLINE%&cTry again later!")
                .replace("%NEWLINE%", "\n").replace("%TIMER%", plugin.formatedTimer());
    }

    public String getLanguage() {
        return languageName;
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    protected void reloadExtraConfigs() throws IOException {
    }

    protected void createExtraFiles() {
    }

    protected void loadExtraSettings() {
    }
}
