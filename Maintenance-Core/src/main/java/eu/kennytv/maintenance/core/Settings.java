package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.ISettings;

import java.util.*;

public abstract class Settings implements ISettings {
    private static final Random RANDOM = new Random();
    protected final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    private final MaintenanceModePlugin plugin;
    protected boolean maintenance;
    private Set<Integer> broadcastIntervalls;
    private List<String> pingMessages;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private String kickMessage;
    private boolean customPlayerCountMessage;
    private boolean customMaintenanceIcon;
    private boolean joinNotifications;

    protected Settings(final MaintenanceModePlugin plugin) {
        this.plugin = plugin;
    }

    protected void loadSettings() {
        updateConfig();

        pingMessages = getConfigList("pingmessages");
        maintenance = getConfigBoolean("enable-maintenance-mode");
        customPlayerCountMessage = getConfigBoolean("enable-playercountmessage");
        customMaintenanceIcon = getConfigBoolean("custom-maintenance-icon");
        joinNotifications = getConfigBoolean("send-join-notification");
        broadcastIntervalls = new HashSet<>(getConfigIntList("timer-broadcast-for-seconds"));
        playerCountMessage = getColoredString(getConfigString("playercountmessage"));
        playerCountHoverMessage = getColoredString(getConfigString("playercounthovermessage"));
        kickMessage = getColoredString(getConfigString("kickmessage"));
        if (customMaintenanceIcon)
            reloadMaintenanceIcon();

        loadExtraSettings();
    }

    private void updateConfig() {
        boolean fileChanged = false;

        // 2.3 pingmessage -> pingmessages
        if (configContains("pingmessage")) {
            final List<String> list = new ArrayList<>();
            list.add(getConfigString("pingmessage"));
            setToConfig("pingmessages", list);
            setToConfig("pingmessage", null);
            fileChanged = true;
        }
        // 2.4 enable-playercountmessage
        if (!configContains("enable-playercountmessage")) {
            setToConfig("enable-playercountmessage", true);
            fileChanged = true;
        }
        // 2.4. timer-broadcasts-for-minutes -> timer-broadcast-for-seconds
        if (configContains("timer-broadcasts-for-minutes") || !configContains("timer-broadcast-for-seconds")) {
            setToConfig("timer-broadcast-for-seconds", Arrays.asList(1200, 900, 600, 300, 120, 60, 30, 20, 10, 5, 4, 3, 2, 1));
            setToConfig("timer-broadcasts-for-minutes", null);
            fileChanged = true;
        }

        if (updateExtraConfig() || fileChanged) {
            saveConfig();
            reloadConfigs();
        }
    }

    public boolean updateExtraConfig() {
        return false;
    }

    public abstract void saveWhitelistedPlayers();

    public abstract String getConfigString(String path);

    public abstract String getMessage(String path);

    public abstract boolean getConfigBoolean(String path);

    public abstract List<Integer> getConfigIntList(String path);

    public abstract List<String> getConfigList(String path);

    public abstract void loadExtraSettings();

    public abstract void setWhitelist(String uuid, String s);

    public abstract void saveConfig();

    public abstract void reloadConfigs();

    public abstract void setToConfig(String path, Object var);

    public abstract boolean configContains(String path);

    public abstract String getColoredString(String s);

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

    public void setMaintenance(final boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getRandomPingMessage() {
        if (pingMessages.isEmpty()) return "";
        final String s = pingMessages.size() > 1 ? pingMessages.get(RANDOM.nextInt(pingMessages.size())) : pingMessages.get(0);
        return getColoredString(s.replace("%NEWLINE%", "\n").replace("%TIMER%", plugin.formatedTimer()));
    }

    public boolean hasCustomPlayerCountMessage() {
        return customPlayerCountMessage;
    }

    @Override
    public boolean isMaintenance() {
        return maintenance;
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
    public Map<UUID, String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (!whitelistedPlayers.containsKey(uuid)) return false;
        whitelistedPlayers.remove(uuid);
        setWhitelist(uuid.toString(), null);
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
        setWhitelist(uuid.toString(), null);
        saveWhitelistedPlayers();
        return true;
    }

    @Override
    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        final boolean contains = !whitelistedPlayers.containsKey(uuid);
        whitelistedPlayers.put(uuid, name);
        setWhitelist(uuid.toString(), name);
        saveWhitelistedPlayers();
        return contains;
    }
}
