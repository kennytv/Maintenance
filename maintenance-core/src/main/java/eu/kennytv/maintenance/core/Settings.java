package eu.kennytv.maintenance.core;

import eu.kennytv.lib.config.Configuration;
import eu.kennytv.lib.config.YamlConfiguration;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.core.listener.IPingListener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public abstract class Settings implements ISettings {
    private static final Random RANDOM = new Random();
    protected final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final MaintenanceModePlugin plugin;
    protected boolean maintenance;
    protected IPingListener pingListener;
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

    protected Settings(final MaintenanceModePlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        createFile(getConfigName());
        createFile("WhitelistedPlayers.yml");
        createExtraFiles();
    }

    @Override
    public void reloadConfigs() {
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), getConfigName())), StandardCharsets.UTF_8));
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
        final File file = new File(plugin.getDataFolder(), getConfigName());
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save " + getConfigName() + "!", e);
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

    protected void createLanguageFile() {
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

    protected void loadSettings() {
        updateConfig();

        pingMessages = config.getStringList("pingmessages");
        maintenance = config.getBoolean("enable-maintenance-mode");
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
            reloadMaintenanceIcon();
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

    protected void saveFile(final Configuration config, final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, file);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to save " + name + "!", e);
        }
    }

    protected void saveWhitelistedPlayers() {
        saveFile(whitelist, "WhitelistedPlayers.yml");
    }

    public String getConfigString(final String path) {
        if (!config.contains(path)) {
            plugin.getLogger().warning("The config is missing the following string: " + path);
            return "";
        }
        return config.getString(path);
    }

    public String getMessage(final String path) {
        if (!language.contains(path)) {
            plugin.getLogger().warning("The language file is missing the following string: " + path);
            return "";
        }
        return getColoredString(language.getString(path));
    }

    public String getRandomPingMessage() {
        if (pingMessages.isEmpty()) return "";
        final String s = pingMessages.size() > 1 ? pingMessages.get(RANDOM.nextInt(pingMessages.size())) : pingMessages.get(0);
        return getColoredString(s.replace("%NEWLINE%", "\n").replace("%TIMER%", plugin.formatedTimer()));
    }

    @Override
    public boolean reloadMaintenanceIcon() {
        return pingListener.loadIcon();
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

    public abstract String getColoredString(String s);

    protected abstract String getConfigName();
}
