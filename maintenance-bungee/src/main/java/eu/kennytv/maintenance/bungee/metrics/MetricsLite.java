package eu.kennytv.maintenance.bungee.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public final class MetricsLite {

    public static final int B_STATS_VERSION = 1;
    private static final String URL = "https://bStats.org/submitData/bungeecord";
    private final Plugin plugin;
    private boolean enabled;
    private String serverUUID;
    private boolean logFailedRequests;
    private static boolean logSentData;
    private static boolean logResponseStatusText;
    private static final List<Object> knownMetricsInstances = new ArrayList<>();

    public MetricsLite(final Plugin plugin) {
        this.plugin = plugin;

        try {
            loadConfig();
        } catch (final IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load bStats config!", e);
            return;
        }

        if (!enabled) {
            return;
        }

        final Class<?> usedMetricsClass = getFirstBStatsClass();
        if (usedMetricsClass == null) {
            return;
        }
        if (usedMetricsClass == getClass()) {
            linkMetrics(this);
            startSubmitting();
        } else {
            try {
                usedMetricsClass.getMethod("linkMetrics", Object.class).invoke(null, this);
            } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                if (logFailedRequests) {
                    plugin.getLogger().log(Level.WARNING, "Failed to link to first metrics class " + usedMetricsClass.getName() + "!", e);
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static void linkMetrics(final Object metrics) {
        knownMetricsInstances.add(metrics);
    }

    public JsonObject getPluginData() {
        final JsonObject data = new JsonObject();

        final String pluginName = plugin.getDescription().getName();
        final String pluginVersion = plugin.getDescription().getVersion();

        data.addProperty("pluginName", pluginName);
        data.addProperty("pluginVersion", pluginVersion);

        final JsonArray customCharts = new JsonArray();
        data.add("customCharts", customCharts);

        return data;
    }

    private void startSubmitting() {
        plugin.getProxy().getScheduler().schedule(plugin, this::submitData, 2, 30, TimeUnit.MINUTES);
    }

    private JsonObject getServerData() {
        int playerAmount = plugin.getProxy().getOnlineCount();
        playerAmount = playerAmount > 500 ? 500 : playerAmount;
        final int onlineMode = plugin.getProxy().getConfig().isOnlineMode() ? 1 : 0;
        final String bungeecordVersion = plugin.getProxy().getVersion();
        final int managedServers = plugin.getProxy().getServers().size();

        final String javaVersion = System.getProperty("java.version");
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");
        final String osVersion = System.getProperty("os.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();

        final JsonObject data = new JsonObject();

        data.addProperty("serverUUID", serverUUID);

        data.addProperty("playerAmount", playerAmount);
        data.addProperty("managedServers", managedServers);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bungeecordVersion", bungeecordVersion);

        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);

        return data;
    }

    private void submitData() {
        final JsonObject data = getServerData();

        final JsonArray pluginData = new JsonArray();
        for (final Object metrics : knownMetricsInstances) {
            try {
                final Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
                if (plugin instanceof JsonObject) {
                    pluginData.add((JsonObject) plugin);
                }
            } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        data.add("plugins", pluginData);

        try {
            sendData(plugin, data);
        } catch (final Exception e) {
            if (logFailedRequests) {
                plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats!", e);
            }
        }
    }

    private void loadConfig() throws IOException {
        final Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
        configPath.toFile().mkdirs();
        final File configFile = new File(configPath.toFile(), "config.yml");
        if (!configFile.exists()) {
            writeFile(configFile,
                    "#bStats collects some data for plugin authors like how many servers are using their plugins.",
                    "#To honor their work, you should not disable it.",
                    "#This has nearly no effect on the server performance!",
                    "#Check out https://bStats.org/ to learn more :)",
                    "enabled: true",
                    "serverUuid: \"" + UUID.randomUUID().toString() + "\"",
                    "logFailedRequests: false",
                    "logSentData: false",
                    "logResponseStatusText: false");
        }

        final Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        enabled = configuration.getBoolean("enabled", true);
        serverUUID = configuration.getString("serverUuid");
        logFailedRequests = configuration.getBoolean("logFailedRequests", false);
        logSentData = configuration.getBoolean("logSentData", false);
        logResponseStatusText = configuration.getBoolean("logResponseStatusText", false);
    }

    private Class<?> getFirstBStatsClass() {
        final Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
        configPath.toFile().mkdirs();
        final File tempFile = new File(configPath.toFile(), "temp.txt");

        try {
            final String className = readFile(tempFile);
            if (className != null) {
                try {
                    return Class.forName(className);
                } catch (final ClassNotFoundException ignored) {
                }
            }
            writeFile(tempFile, getClass().getName());
            return getClass();
        } catch (final IOException e) {
            if (logFailedRequests) {
                plugin.getLogger().log(Level.WARNING, "Failed to get first bStats class!", e);
            }
            return null;
        }
    }

    private String readFile(final File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (
                final FileReader fileReader = new FileReader(file);
                final BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            return bufferedReader.readLine();
        }
    }

    private void writeFile(final File file, final String... lines) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (
                final FileWriter fileWriter = new FileWriter(file);
                final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
        ) {
            for (final String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
    }

    private static void sendData(final Plugin plugin, final JsonObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (logSentData) {
            plugin.getLogger().info("Sending data to bStats: " + data.toString());
        }

        final HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

        final byte[] compressedData = compress(data.toString());

        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

        connection.setDoOutput(true);
        final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(compressedData);
        outputStream.flush();
        outputStream.close();

        if (logResponseStatusText) {
            final InputStream inputStream = connection.getInputStream();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            plugin.getLogger().info("Sent data to bStats and received response: " + builder.toString());
        } else {
            connection.getInputStream().close();
        }
    }

    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
        gzip.close();
        return outputStream.toByteArray();
    }

}