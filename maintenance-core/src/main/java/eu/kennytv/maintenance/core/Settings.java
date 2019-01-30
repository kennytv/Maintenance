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

import eu.kennytv.lib.config.Configuration;
import eu.kennytv.lib.config.YamlConfiguration;
import eu.kennytv.maintenance.api.ISettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Settings implements ISettings {
    private static final Random RANDOM = new Random();
    protected final MaintenanceModePlugin plugin;
    private final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final String configName;
    protected boolean maintenance;
    private Set<Integer> broadcastIntervalls;
    private List<String> pingMessages;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private String kickMessage;
    private String languageName;
    private boolean customPlayerCountMessage;
    private boolean customMaintenanceIcon;
    private boolean joinNotifications;
    private boolean debug;

    protected Configuration config;
    protected Configuration language;
    protected Configuration whitelist;

    public Settings(final MaintenanceModePlugin plugin, final String configName) {
        this.plugin = plugin;
        this.configName = configName;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        createFile(configName);
        createFile("WhitelistedPlayers.yml");
        createExtraFiles();

        reloadConfigs();
    }

    @Override
    public void reloadConfigs() {
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), configName)), StandardCharsets.UTF_8));
            whitelist = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "WhitelistedPlayers.yml"));
            reloadExtraConfigs();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance files!", e);
        }

        loadSettings();
        createLanguageFile();

        try {
            language = YamlConfiguration.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), "language-" + languageName + ".yml")), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load Maintenance language file!", e);
        }
    }

    public void saveConfig() {
        final File file = new File(plugin.getDataFolder(), configName);
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save " + configName + "!", e);
        }
    }

    protected void saveFile(final Configuration config, final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, file);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save " + name + "!", e);
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
        saveFile(whitelist, "WhitelistedPlayers.yml");
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
        kickMessage = getColoredString(getConfigString("kickmessage"));
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
        boolean fileChanged = false;

        // 2.3 pingmessage -> pingmessages
        if (config.contains("pingmessage")) {
            final List<String> list = new ArrayList<>();
            list.add(getConfigString("pingmessage"));
            config.set("pingmessages", list);
            config.set("pingmessage", null);
            fileChanged = true;
        }
        // 2.4 enable-playercountmessage
        if (!config.contains("enable-playercountmessage")) {
            config.set("enable-playercountmessage", true);
            fileChanged = true;
        }
        // 2.4. timer-broadcasts-for-minutes -> timer-broadcast-for-seconds
        if (config.contains("timer-broadcasts-for-minutes") || !config.contains("timer-broadcast-for-seconds")) {
            config.set("timer-broadcast-for-seconds", Arrays.asList(1200, 900, 600, 300, 120, 60, 30, 20, 10, 5, 4, 3, 2, 1));
            config.set("timer-broadcasts-for-minutes", null);
            fileChanged = true;
        }
        // 2.5 language
        if (!config.contains("language")) {
            config.set("language", "en");
        }

        if (updateExtraConfig() || fileChanged) {
            plugin.getLogger().info("Updated config to the latest version!");
            saveConfig();
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
        if (!config.contains(path)) {
            plugin.getLogger().warning("The config is missing the following string: " + path);
            return "null";
        }
        return config.getString(path);
    }

    public String getMessage(final String path) {
        if (!language.contains(path)) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return "null";
        }
        return getColoredString(language.getString(path));
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

    public Configuration getConfig() {
        return config;
    }

    public Configuration getWhitelist() {
        return whitelist;
    }

    public List<String> getPingMessages() {
        return pingMessages;
    }

    public Set<Integer> getBroadcastIntervalls() {
        return broadcastIntervalls;
    }

    public String getPlayerCountMessage() {
        return playerCountMessage;
    }

    public String getPlayerCountHoverMessage() {
        return playerCountHoverMessage;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getLanguage() {
        return languageName;
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    protected boolean updateExtraConfig() {
        return false;
    }

    protected void reloadExtraConfigs() throws IOException {
    }

    protected void createExtraFiles() {
    }

    protected void loadExtraSettings() {
    }
}
