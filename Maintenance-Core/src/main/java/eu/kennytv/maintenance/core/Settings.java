package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.ISettings;

import java.util.*;

public abstract class Settings implements ISettings {
    protected final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    protected boolean maintenance;
    private Set<Integer> broadcastIntervalls;
    private String timerBroadcastMessage;
    private String endtimerBroadcastMessage;
    private String pingMessage;
    private String kickMessage;
    private String joinNotification;
    private String noPermMessage;
    private String maintenanceActivated;
    private String maintenanceDeactivated;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private boolean joinNotifications;
    private boolean customMaintenanceIcon;

    protected void loadSettings() {
        timerBroadcastMessage = getConfigString("starttimer-broadcast-mesage");
        endtimerBroadcastMessage = getConfigString("endtimer-broadcast-mesage");
        pingMessage = getConfigString("pingmessage");
        kickMessage = getConfigString("kickmessage");
        joinNotification = getConfigString("join-notification");
        noPermMessage = getConfigString("no-permission");
        maintenanceActivated = getConfigString("maintenance-activated");
        maintenanceDeactivated = getConfigString("maintenance-deactivated");
        maintenance = getConfigBoolean("enable-maintenance-mode");
        joinNotifications = getConfigBoolean("send-join-notification");
        customMaintenanceIcon = getConfigBoolean("custom-maintenance-icon");
        broadcastIntervalls = new HashSet<>(getBroadcastIntervallList());
        playerCountMessage = getConfigString("playercountmessage");
        playerCountHoverMessage = getConfigString("playercounthovermessage");
        if (hasCustomIcon())
            reloadMaintenanceIcon();

        loadExtraSettings();
    }

    public abstract void saveWhitelistedPlayers();

    public abstract boolean createFiles();

    public abstract String getConfigString(String path);

    public abstract String getRawConfigString(String path);

    public abstract boolean getConfigBoolean(String path);

    public abstract List<Integer> getBroadcastIntervallList();

    public abstract void loadExtraSettings();

    public abstract void setWhitelist(String uuid, String s);

    public abstract void saveConfig();

    public abstract void reloadConfigs();

    public abstract void setToConfig(String path, Object var);

    public String getTimerBroadcastMessage() {
        return timerBroadcastMessage;
    }

    public String getEndtimerBroadcastMessage() {
        return endtimerBroadcastMessage;
    }

    public String getPingMessage() {
        return pingMessage;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getJoinNotification() {
        return joinNotification;
    }

    public String getNoPermMessage() {
        return noPermMessage;
    }

    public String getMaintenanceActivated() {
        return maintenanceActivated;
    }

    public String getMaintenanceDeactivated() {
        return maintenanceDeactivated;
    }

    public String getPlayerCountMessage() {
        return playerCountMessage;
    }

    public String getPlayerCountHoverMessage() {
        return playerCountHoverMessage;
    }

    public void setMaintenance(final boolean maintenance) {
        this.maintenance = maintenance;
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

    public Set<Integer> getBroadcastIntervalls() {
        return broadcastIntervalls;
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
        if (!whitelistedPlayers.containsValue(name)) return false;
        final UUID uuid = whitelistedPlayers.entrySet().stream().filter(entry -> entry.getValue().equals(name)).findFirst().get().getKey();
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
