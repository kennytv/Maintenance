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

import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.ISettings;
import eu.kennytv.maintenance.core.dump.MaintenanceDump;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.core.util.Version;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MaintenanceModePlugin implements IMaintenance {
    protected final Version version;
    protected Settings settings;
    protected ServerListPlusHook serverListPlusHook;
    protected MaintenanceRunnable runnable;
    private final String prefix;
    private final ServerType serverType;
    private Version newestVersion;
    private Task task;

    // TODO starttimer -> start, endtimer -> end, timer abort -> cancel (?)

    protected MaintenanceModePlugin(final String version, final ServerType serverType) {
        this.version = new Version(version);
        this.serverType = serverType;
        this.prefix = "§8[§eMaintenance" + serverType + "§8] ";
        checkNewestVersion();
    }

    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.getConfig().set("maintenance-enabled", maintenance);
        settings.saveConfig();
        serverActions(maintenance);
    }

    public void serverActions(final boolean maintenance) {
        if (isTaskRunning())
            cancelTask();
        if (serverListPlusHook != null)
            serverListPlusHook.setEnabled(!maintenance);

        if (maintenance) {
            kickPlayers();
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));
    }

    public String formatedTimer() {
        if (!isTaskRunning()) return "-";
        final int preHours = runnable.getSecondsLeft() / 60;
        final int minutes = preHours % 60;
        final int seconds = runnable.getSecondsLeft() % 60;
        return String.format("%02d:%02d:%02d", preHours / 60, minutes, seconds);
    }

    public void startMaintenanceRunnable(final int minutes, final boolean enable) {
        runnable = new MaintenanceRunnable(this, settings, minutes, enable);
        task = startMaintenanceRunnable(runnable);
    }

    public boolean updateAvailable() {
        checkNewestVersion();
        return version.compareTo(newestVersion) == -1;
    }

    protected void sendEnableMessage() {
        final String updateMessage;
        if (version.compareTo(newestVersion) == -1) {
            updateMessage = "§cNewest version available: §aVersion " + newestVersion + "§c, you're on §a" + version;
        } else if (version.compareTo(newestVersion) != 0) {
            if (version.getTag().equalsIgnoreCase("snapshot")) {
                updateMessage = "§cYou're running a development version, please report bugs on the Discord server (https://kennytv.eu/discord) or the GitHub issue tracker (https://kennytv.eu/maintenance/issues)";
            } else {
                updateMessage = "§cYou're running a version, that doesn't exist! §cN§ai§dc§ee§5!";
            }
        } else
            updateMessage = "You have the latest version of the plugin installed.";
        getLogger().info("Plugin by KennyTV");
        getLogger().info(updateMessage);
    }

    public boolean installUpdate() {
        try {
            final URLConnection conn = new URL("https://github.com/KennyTV/Maintenance/releases/download/" + newestVersion + "/Maintenance.jar").openConnection();
            writeFile(new BufferedInputStream(conn.getInputStream()), new BufferedOutputStream(new FileOutputStream(getPluginFolder() + "Maintenance.tmp")));
            final File file = new File(getPluginFolder() + "Maintenance.tmp");
            final long newlength = file.length();
            if (newlength < 10000) {
                file.delete();
                return false;
            }

            writeFile(new FileInputStream(file), new BufferedOutputStream(new FileOutputStream(getPluginFile())));
            file.delete();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void writeFile(final InputStream is, final OutputStream os) throws IOException {
        final byte[] chunk = new byte[1024];
        int chunkSize;
        while ((chunkSize = is.read(chunk)) != -1) {
            os.write(chunk, 0, chunkSize);
        }
        is.close();
        os.close();
    }

    private void checkNewestVersion() {
        try {
            final HttpURLConnection c = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=40699").openConnection();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            final String newVersionString = reader.readLine();
            reader.close();
            final Version newVersion = new Version(newVersionString);
            if (!newVersion.equals(version))
                newestVersion = newVersion;
        } catch (final Exception ignored) {
        }
    }

    public String pasteDump() {
        final MaintenanceDump dump = new MaintenanceDump(this, settings);
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://hastebin.com/documents").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Maintenance/" + getVersion());
            connection.setRequestProperty("Content-Type", "text/plain");

            final GsonBuilder gsonBuilder = new GsonBuilder();
            final byte[] bytes = gsonBuilder.setPrettyPrinting().create().toJson(dump).getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

            final OutputStream out = connection.getOutputStream();
            out.write(bytes);
            out.close();

            if (connection.getResponseCode() == 503) {
                getLogger().warning("Could not paste dump, Hastebin services are probably down (?)");
                return null;
            }

            final InputStream in = connection.getInputStream();
            final String output = CharStreams.toString(new InputStreamReader(in));
            in.close();

            final JsonObject jsonOutput = gsonBuilder.create().fromJson(output, JsonObject.class);
            if (!jsonOutput.has("key")) {
                getLogger().log(Level.WARNING, "Could not paste dump, there was no key returned :(");
                return null;
            }

            return jsonOutput.get("key").getAsString();
        } catch (final IOException e) {
            getLogger().log(Level.WARNING, "Could not paste dump :(", e);
            return null;
        }
    }

    public void loadMaintenanceIcon() {
        final File file = new File(getDataFolder(), "maintenance-icon.png");
        if (!file.exists()) {
            getLogger().warning("Could not find a 'maintenance-icon.png' file - did you create one in the plugin's folder?");
            return;
        }

        try {
            loadIcon(file);
        } catch (final Exception e) {
            getLogger().log(Level.WARNING, "Could not load the 'maintenance-icon.png' file!", e);
            e.printStackTrace();
        }
    }

    public void cancelTask() {
        task.cancel();
        runnable = null;
        task = null;
    }

    public List<String> getMaintenanceServers() {
        return isMaintenance() ? Arrays.asList("\"global\"") : null;
    }

    @Override
    public boolean isMaintenance() {
        return settings.isMaintenance();
    }

    @Override
    public boolean isTaskRunning() {
        return task != null && runnable != null;
    }

    @Override
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }

    public Version getNewestVersion() {
        return newestVersion;
    }

    public String getPrefix() {
        return prefix;
    }

    public MaintenanceRunnable getRunnable() {
        return runnable;
    }

    public ServerType getServerType() {
        return serverType;
    }

    protected String getPluginFolder() {
        return "plugins/";
    }

    public abstract void async(Runnable runnable);

    public abstract void broadcast(String message);

    public abstract void sendUpdateNotification(SenderInfo sender);

    public abstract SenderInfo getPlayer(String name);

    public abstract SenderInfo getOfflinePlayer(UUID uuid);

    public abstract File getDataFolder();

    public abstract InputStream getResource(String name);

    public abstract Logger getLogger();

    public abstract String getServerVersion();

    public abstract List<PluginDump> getPlugins();

    protected abstract void loadIcon(File file) throws Exception;

    protected abstract void kickPlayers();

    protected abstract Task startMaintenanceRunnable(Runnable runnable);

    protected abstract File getPluginFile();
}
