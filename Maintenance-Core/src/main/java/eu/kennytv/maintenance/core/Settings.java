package eu.kennytv.maintenance.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Settings {
    protected final Map<UUID, String> whitelistedPlayers = Maps.newHashMap();
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
        broadcastIntervalls = Sets.newHashSet(getBroadcastIntervallList());
        playerCountMessage = getConfigString("playercountmessage");
        playerCountHoverMessage = getConfigString("playercounthovermessage");

        loadExtraSettings();
    }

    public abstract void saveWhitelistedPlayers();

    public abstract boolean createFiles();

    public abstract String getConfigString(String path);

    public abstract boolean getConfigBoolean(String path);

    public abstract List<Integer> getBroadcastIntervallList();

    public abstract void loadExtraSettings();

    public abstract void setWhitelist(String uuid, String s);

    public abstract void saveConfig();

    public abstract void reloadConfigs();

    public abstract void setConfigBoolean(String path, boolean boo);

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

    public boolean isMaintenance() {
        return maintenance;
    }

    public boolean isJoinNotifications() {
        return joinNotifications;
    }

    public boolean hasCustomIcon() {
        return customMaintenanceIcon;
    }

    public Set<Integer> getBroadcastIntervalls() {
        return broadcastIntervalls;
    }

    public Map<UUID, String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (!whitelistedPlayers.containsKey(uuid)) return false;
        whitelistedPlayers.remove(uuid);
        setWhitelist(uuid.toString(), null);
        saveWhitelistedPlayers();
        return true;
    }

    @Deprecated
    public boolean removeWhitelistedPlayer(final String name) {
        if (!whitelistedPlayers.containsValue(name)) return false;
        final UUID uuid = whitelistedPlayers.entrySet().stream().filter(entry -> entry.getValue().equals(name)).findFirst().get().getKey();
        whitelistedPlayers.remove(uuid);
        setWhitelist(uuid.toString(), null);
        saveWhitelistedPlayers();
        return true;
    }

    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        final boolean contains = !whitelistedPlayers.containsKey(uuid);
        whitelistedPlayers.put(uuid, name);
        setWhitelist(uuid.toString(), name);
        saveWhitelistedPlayers();
        return contains;
    }
}
