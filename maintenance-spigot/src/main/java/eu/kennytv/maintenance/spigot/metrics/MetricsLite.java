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

package eu.kennytv.maintenance.spigot.metrics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public final class MetricsLite {

    public static final int B_STATS_VERSION = 1;
    private static final String URL = "https://bStats.org/submitData/bukkit";
    private boolean enabled;
    private static boolean logFailedRequests;
    private static boolean logSentData;
    private static boolean logResponseStatusText;
    private static String serverUUID;
    private final Plugin plugin;

    public MetricsLite(final Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;

        final File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        final File configFile = new File(bStatsFolder, "config.yml");
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);

            config.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
                            "To honor their work, you should not disable it.\n" +
                            "This has nearly no effect on the server performance!\n" +
                            "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }

        serverUUID = config.getString("serverUuid");
        logFailedRequests = config.getBoolean("logFailedRequests", false);
        logSentData = config.getBoolean("logSentData", false);
        logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        enabled = config.getBoolean("enabled", true);
        if (enabled) {
            boolean found = false;
            for (final Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION");
                    found = true;
                    break;
                } catch (final NoSuchFieldException ignored) {
                }
            }
            Bukkit.getServicesManager().register(MetricsLite.class, this, plugin, ServicePriority.Normal);
            if (!found) {
                startSubmitting();
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void startSubmitting() {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    timer.cancel();
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> submitData());
            }
        }, 1000 * 60 * 5, 1000 * 60 * 30);
    }

    public JSONObject getPluginData() {
        final JSONObject data = new JSONObject();

        final String pluginName = plugin.getDescription().getName();
        final String pluginVersion = plugin.getDescription().getVersion();

        data.put("pluginName", pluginName);
        data.put("pluginVersion", pluginVersion);
        final JSONArray customCharts = new JSONArray();
        data.put("customCharts", customCharts);

        return data;
    }

    private JSONObject getServerData() {
        final int playerAmount = Bukkit.getOnlinePlayers().size();
        final int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        final String bukkitVersion = Bukkit.getVersion();

        final String javaVersion = System.getProperty("java.version");
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");
        final String osVersion = System.getProperty("os.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();

        final JSONObject data = new JSONObject();

        data.put("serverUUID", serverUUID);

        data.put("playerAmount", playerAmount);
        data.put("onlineMode", onlineMode);
        data.put("bukkitVersion", bukkitVersion);

        data.put("javaVersion", javaVersion);
        data.put("osName", osName);
        data.put("osArch", osArch);
        data.put("osVersion", osVersion);
        data.put("coreCount", coreCount);

        return data;
    }

    private void submitData() {
        final JSONObject data = getServerData();

        final JSONArray pluginData = new JSONArray();
        for (final Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION");

                for (final RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
                    try {
                        pluginData.add(provider.getService().getMethod("getPluginData").invoke(provider.getProvider()));
                    } catch (final NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                    }
                }
            } catch (final NoSuchFieldException ignored) {
            }
        }

        data.put("plugins", pluginData);

        new Thread(() -> {
            try {
                sendData(plugin, data);
            } catch (final Exception e) {
                if (logFailedRequests) {
                    plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
                }
            }
        }).start();
    }

    private static void sendData(final Plugin plugin, final JSONObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        }
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("This method must not be called from the main thread!");
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