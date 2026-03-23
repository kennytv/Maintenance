package eu.kennytv.maintenance.core.proxy.redis;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

public final class RedisHandler {

    private static final String REDIS_STREAM_KEY = "maintenance:stream";
    private static final String REDIS_MAINTENANCE_KEY = "maintenance:status";
    private static final String REDIS_MODE_KEY = "maintenance:mode";
    private static final String REDIS_SERVERS_KEY = "maintenance:servers";
    private static final String REDIS_SERVER_MODES_KEY = "maintenance:server_modes";
    private static final String REDIS_WHITELIST_KEY = "maintenance:whitelist";

    private static final String MSG_TYPE_MAINTENANCE_UPDATE = "status";
    private static final String MSG_TYPE_SERVER_ADD = "server_add";
    private static final String MSG_TYPE_SERVER_REMOVE = "server_remove";
    private static final String MSG_TYPE_PLAYER_ADD = "player_add";
    private static final String MSG_TYPE_PLAYER_REMOVE = "player_remove";
    private static final String MSG_TYPE = "type";
    private static final String MSG_VALUE = "val";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MaintenanceProxyPlugin plugin;
    private final SettingsProxy settings;
    private RedisClient client;
    private StreamEntryID lastStreamId = new StreamEntryID();

    public RedisHandler(final MaintenanceProxyPlugin plugin, final SettingsProxy settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void setup(final String redisUri) {
        plugin.getLogger().info("Trying to open Redis connection...");

        final ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxWait(Duration.ofSeconds(5));
        poolConfig.setMaxTotal(2);
        client = RedisClient.builder().poolConfig(poolConfig).fromURI(URI.create(redisUri)).build();

        // Get the latest stream ID to only receive new messages
        final List<StreamEntry> messages = client.xrevrange(REDIS_STREAM_KEY, (StreamEntryID) null, null, 1);
        if (!messages.isEmpty()) {
            lastStreamId = messages.getFirst().getID();
        }

        running.set(true);
        plugin.async(this::listenToStream);

        plugin.getLogger().info("Connected to Redis!");
    }

    public boolean loadMaintenanceStatus() {
        return Boolean.parseBoolean(client.get(REDIS_MAINTENANCE_KEY));
    }

    public @Nullable String loadGlobalMode() {
        return client.get(REDIS_MODE_KEY);
    }

    public Map<String, String> loadMaintenanceServers() {
        final Set<String> servers = client.smembers(REDIS_SERVERS_KEY);
        final Map<String, String> serverModes = new HashMap<>();
        if (servers == null) {
            return serverModes;
        }

        final Map<String, String> persistedModes = client.hgetAll(REDIS_SERVER_MODES_KEY);
        for (final String server : servers) {
            serverModes.put(server, normalizeMode(persistedModes.get(server)));
        }
        return serverModes;
    }

    public void loadPlayers(final Map<UUID, String> players) {
        for (final Map.Entry<String, String> entry : client.hgetAll(REDIS_WHITELIST_KEY).entrySet()) {
            players.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
    }

    public void set(final boolean maintenance, final String mode) {
        client.set(REDIS_MAINTENANCE_KEY, Boolean.toString(maintenance));
        client.set(REDIS_MODE_KEY, mode);
        client.xadd(REDIS_STREAM_KEY, StreamEntryID.NEW_ENTRY, Map.of(
                MSG_TYPE, MSG_TYPE_MAINTENANCE_UPDATE,
                MSG_VALUE, Boolean.toString(maintenance),
                "mode", mode
        ));
    }

    public boolean addServer(final String server, final String mode) {
        if (client.sadd(REDIS_SERVERS_KEY, server) < 1) {
            return false;
        }
        client.hset(REDIS_SERVER_MODES_KEY, server, mode);
        client.xadd(REDIS_STREAM_KEY, StreamEntryID.NEW_ENTRY, Map.of(
                MSG_TYPE, MSG_TYPE_SERVER_ADD,
                MSG_VALUE, server,
                "mode", mode
        ));
        return true;
    }

    public void setServerMode(final String server, final String mode) {
        client.hset(REDIS_SERVER_MODES_KEY, server, mode);
        client.xadd(REDIS_STREAM_KEY, StreamEntryID.NEW_ENTRY, Map.of(
                MSG_TYPE, MSG_TYPE_SERVER_ADD,
                MSG_VALUE, server,
                "mode", mode
        ));
    }

    public boolean removeServer(final String server) {
        if (client.srem(REDIS_SERVERS_KEY, server) < 1) {
            return false;
        }
        client.hdel(REDIS_SERVER_MODES_KEY, server);
        send(MSG_TYPE_SERVER_REMOVE, server);
        return true;
    }

    public void addPlayer(final UUID uuid, final String name) {
        final String value = uuid.toString();
        client.hset(REDIS_WHITELIST_KEY, value, name);
        client.xadd(REDIS_STREAM_KEY, StreamEntryID.NEW_ENTRY, Map.of(
                MSG_TYPE, MSG_TYPE_PLAYER_ADD,
                MSG_VALUE, value,
                "name", name
        ));
    }

    public void removePlayer(final UUID uuid) {
        final String value = uuid.toString();
        client.hdel(REDIS_WHITELIST_KEY, value);
        send(MSG_TYPE_PLAYER_REMOVE, value);
    }

    private void send(final String type, final String value) {
        client.xadd(REDIS_STREAM_KEY, StreamEntryID.NEW_ENTRY, Map.of(MSG_TYPE, type, MSG_VALUE, value));
    }

    private void listenToStream() {
        while (running.get()) {
            try {
                final List<Map.Entry<String, List<StreamEntry>>> result = client.xread(
                        XReadParams.xReadParams().block(5000),
                        Map.of(REDIS_STREAM_KEY, lastStreamId)
                );
                if (result != null) {
                    for (final Map.Entry<String, List<StreamEntry>> streamEntries : result) {
                        for (final StreamEntry entry : streamEntries.getValue()) {
                            plugin.sync(() -> processStreamMessage(entry));
                            lastStreamId = entry.getID();
                        }
                    }
                }
            } catch (final Exception e) {
                if (running.get()) {
                    plugin.getLogger().log(Level.WARNING, "Error reading from Redis stream", e);
                    try {
                        Thread.sleep(1000); // retry
                    } catch (final InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void processStreamMessage(final StreamEntry message) {
        final Map<String, String> body = message.getFields();
        final String type = body.get(MSG_TYPE);
        if (type == null) {
            return;
        }

        final String value = body.get(MSG_VALUE);
        switch (type) {
            case MSG_TYPE_MAINTENANCE_UPDATE -> handleStatus(value, body.get("mode"));
            case MSG_TYPE_SERVER_ADD -> handleServerStatus(value, body.get("mode"), true, "Server added to maintenance via Redis: ");
            case MSG_TYPE_SERVER_REMOVE -> handleServerStatus(value, null, false, "Server removed from maintenance via Redis: ");
            case MSG_TYPE_PLAYER_ADD -> handlePlayerAdd(value, body.get("name"));
            case MSG_TYPE_PLAYER_REMOVE -> handlePlayerRemove(value);
        }
    }

    private void handleStatus(final String value, @Nullable final String mode) {
        final boolean maintenance = Boolean.parseBoolean(value);
        if (settings.isMaintenance() != maintenance) {
            plugin.getLogger().info("Maintenance status updated via Redis: " + maintenance);
            settings.setMaintenanceDirect(maintenance, mode);
            plugin.serverActions(maintenance);
            return;
        }

        if (maintenance && !normalizeMode(mode).equals(normalizeMode(settings.activeMode()))) {
            settings.setActiveModeDirect(mode);
        }
    }

    private void handleServerStatus(final String value, @Nullable final String mode, final boolean maintenance, final String log) {
        if (value == null) {
            return;
        }

        final boolean success;
        if (maintenance) {
            success = settings.addMaintenanceServerDirect(value, mode);
            settings.setMaintenanceServerModeDirect(value, mode);
        } else {
            success = settings.removeMaintenanceServerDirect(value);
        }

        if (success) {
            plugin.getLogger().info(log + value);
            final Server server = plugin.getServerOrDummy(value);
            plugin.serverActions(server, maintenance);
        }
    }

    private void handlePlayerAdd(final String uuid, final String name) {
        settings.addWhitelistedPlayerDirect(UUID.fromString(uuid), name);
    }

    private void handlePlayerRemove(final String value) {
        settings.removeWhitelistedPlayerDirect(UUID.fromString(value));
    }

    public void close() {
        running.set(false);
        if (client != null) {
            client.close();
            client = null;
        }
    }

    private String normalizeMode(@Nullable final String mode) {
        return mode == null || mode.isBlank() ? "default" : mode.toLowerCase(Locale.ROOT);
    }
}
