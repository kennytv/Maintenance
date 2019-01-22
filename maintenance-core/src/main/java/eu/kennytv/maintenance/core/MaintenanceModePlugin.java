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

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Version;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public abstract class MaintenanceModePlugin implements IMaintenance {
    protected final Version version;
    protected ServerListPlusHook serverListPlusHook;
    protected MaintenanceRunnable runnable;
    protected int taskId;
    private final String prefix;
    private final ServerType serverType;
    private Version newestVersion;

    protected MaintenanceModePlugin(final String version, final ServerType serverType) {
        this.version = new Version(version);
        this.serverType = serverType;
        this.prefix = "§8[§eMaintenance" + serverType + "§8] ";
        checkNewestVersion();
    }

    public String formatedTimer() {
        if (!isTaskRunning()) return "-";
        final int preHours = runnable.getSecondsLeft() / 60;
        final int minutes = preHours % 60;
        final int seconds = runnable.getSecondsLeft() % 60;
        return String.format("%02d:%02d:%02d", preHours / 60, minutes, seconds);
    }

    public String getUpdateMessage() {
        if (version.compareTo(newestVersion) == -1) {
            return "§cNewest version available: §aVersion " + newestVersion + "§c, you're on §a" + version;
        } else if (version.compareTo(newestVersion) != 0) {
            if (version.getTag().equalsIgnoreCase("snapshot")) {
                return "§cYou're running a development version, please report bugs on the Discord server (https://kennytv.eu/discord) or the GitHub issue tracker (https://kennytv.eu/maintenance/issues)";
            } else {
                return "§cYou're running a version, that doesn't exist! §cN§ai§dc§ee§5!";
            }
        }
        return "You have the latest version of the plugin installed.";
    }

    public void checkNewestVersion() {
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

    public boolean updateAvailable() {
        checkNewestVersion();
        return version.compareTo(newestVersion) == -1;
    }

    public void startMaintenanceRunnable(final int minutes, final boolean enable) {
        runnable = new MaintenanceRunnable(this, (Settings) getSettings(), minutes, enable);
        taskId = startMaintenanceRunnable(runnable);
    }

    @Override
    public boolean isMaintenance() {
        return getSettings().isMaintenance();
    }

    @Override
    public boolean isTaskRunning() {
        return taskId != 0 && runnable != null;
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

    protected abstract int startMaintenanceRunnable(Runnable runnable);

    public abstract void async(Runnable runnable);

    public abstract void cancelTask();

    public abstract void broadcast(String message);

    public abstract void sendUpdateNotification(SenderInfo sender);

    public abstract File getDataFolder();

    protected abstract File getPluginFile();

    protected abstract String getPluginFolder();

    public abstract InputStream getResource(String name);

    public abstract Logger getLogger();
}
