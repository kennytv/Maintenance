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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.event.MaintenanceChangedEvent;
import eu.kennytv.maintenance.api.event.manager.IEventManager;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.dump.MaintenanceDump;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.event.EventManager;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MaintenancePlugin implements IMaintenance {
    protected final EventManager eventManager;
    protected final Version version;
    protected Settings settings;
    protected ServerListPlusHook serverListPlusHook;
    protected MaintenanceRunnable runnable;
    protected MaintenanceCommand commandManager;
    private final String prefix;
    private final ServerType serverType;
    private Version newestVersion;

    protected MaintenancePlugin(final String version, final ServerType serverType) {
        this.version = new Version(version);
        this.serverType = serverType;
        this.prefix = "§8[§eMaintenance" + serverType + "§8] ";
        this.eventManager = new EventManager();
    }

    public void disable() {
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.getConfig().set("maintenance-enabled", maintenance);
        settings.saveConfig();
        serverActions(maintenance);
    }

    public void serverActions(final boolean maintenance) {
        if (isTaskRunning()) {
            cancelTask();
        }
        if (serverListPlusHook != null) {
            serverListPlusHook.setEnabled(!maintenance);
        }

        if (maintenance) {
            if (settings.isKickOnlinePlayers()) {
                kickPlayers();
            }
            broadcast(settings.getMessage("maintenanceActivated"));
        } else
            broadcast(settings.getMessage("maintenanceDeactivated"));

        eventManager.callEvent(new MaintenanceChangedEvent(maintenance));
    }

    public String formatedTimer(final String s) {
        return s.contains("%TIMER%") ? s.replace("%TIMER%", formatedTimer()) : s;
    }

    public String formatedTimer() {
        if (!isTaskRunning()) return settings.getMessage("motdTimerNotRunning", "-");
        final int preHours = runnable.getSecondsLeft() / 60;
        final int minutes = preHours % 60;
        final int seconds = runnable.getSecondsLeft() % 60;
        return settings.getMessage("motdTimer", "%HOURS%:%MINUTES%:%SECONDS%")
                .replace("%HOURS%", String.format("%02d", preHours / 60))
                .replace("%MINUTES%", String.format("%02d", minutes))
                .replace("%SECONDS%", String.format("%02d", seconds));
    }

    public void startMaintenanceRunnableForMinutes(final int minutes, final boolean enable) {
        startMaintenanceRunnableForSeconds(minutes * 60, enable);
    }

    public void startMaintenanceRunnableForSeconds(final int seconds, final boolean enable) {
        runnable = new MaintenanceRunnable(this, settings, seconds, enable);
        // Save the endtimer to be able to continue it after a server stop
        if (settings.isSaveEndtimerOnStop() && !runnable.shouldEnable()) {
            settings.setSavedEndtimer(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(runnable.getSecondsLeft()));
        }
    }

    public boolean updateAvailable() {
        checkNewestVersion();
        return version.compareTo(newestVersion) == -1;
    }

    protected void continueLastEndtimer() {
        if (!settings.isSaveEndtimerOnStop()) return;
        if (settings.getSavedEndtimer() == 0) return;

        final long current = System.currentTimeMillis();
        getLogger().info("Found interrupted endtimer from last uptime...");
        if (!isMaintenance()) {
            getLogger().info("Maintenance has already been disabled, thus the timer has been cancelled.");
            settings.setSavedEndtimer(0);
        } else if (settings.getSavedEndtimer() < current) {
            getLogger().info("The endtimer has already expired, maintenance has been disabled.");
            setMaintenance(false);
            settings.setSavedEndtimer(0);
        } else {
            final int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(settings.getSavedEndtimer() - current);
            startMaintenanceRunnableForSeconds(seconds, false);
            getLogger().info("The timer has been continued - maintenance will be disabled in: " + formatedTimer());
        }
    }

    protected void sendEnableMessage() {
        getLogger().info("Plugin by KennyTV");
        if (!settings.hasUpdateChecks()) return;
        async(() -> {
            checkNewestVersion();
            final int compare = version.compareTo(newestVersion);
            if (compare == -1) {
                getLogger().info("§cNewest version available: §aVersion " + newestVersion + "§c, you're on §a" + version);
            } else if (compare == 1) {
                if (version.getTag().equalsIgnoreCase("snapshot")) {
                    getLogger().info("§cYou're running a development version, please report bugs on the Discord server (https://discord.gg/vGCUzHq) or the GitHub issue tracker (https://github.com/KennyTV/Maintenance/issues)");
                } else {
                    getLogger().info("§cYou're running a version, that doesn't exist! §cN§ai§dc§ee§5!");
                }
            }
        });
    }

    public boolean installUpdate() {
        // Ore sad :(
        Preconditions.checkArgument(serverType != ServerType.SPONGE);
        try {
            final String fileSuffix = serverType == ServerType.VELOCITY ? "Velocity" : "";
            final URLConnection conn = new URL("https://github.com/KennyTV/Maintenance/releases/download/" + newestVersion + "/Maintenance" + fileSuffix + ".jar").openConnection();
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
            if (!newVersion.equals(version)) {
                newestVersion = newVersion;
            }
        } catch (final Exception ignored) {
        }
    }

    public String pasteDump() {
        final MaintenanceDump dump = new MaintenanceDump(this, settings);
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://hasteb.in/documents").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Maintenance/" + getVersion());
            connection.setRequestProperty("Content-Type", "text/plain");

            final GsonBuilder gsonBuilder = new GsonBuilder();
            final OutputStream out = connection.getOutputStream();
            out.write(gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create().toJson(dump).getBytes(StandardCharsets.UTF_8));
            out.close();

            if (connection.getResponseCode() == 503) {
                getLogger().warning("Could not paste dump, hasteb.in down?");
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
            getLogger().log(Level.WARNING, "Could not paste dump :(");
            e.printStackTrace();
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
            getLogger().log(Level.WARNING, "Could not load the 'maintenance-icon.png' file!");
            e.printStackTrace();
        }
    }

    public void cancelTask() {
        if (settings.isSaveEndtimerOnStop() && !runnable.shouldEnable())
            settings.setSavedEndtimer(0);
        runnable.getTask().cancel();
        runnable = null;
    }

    public UUID checkUUID(final SenderInfo sender, final String s) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(s);
        } catch (final Exception e) {
            sender.sendMessage(settings.getMessage("invalidUuid"));
            return null;
        }
        return uuid;
    }

    public String[] removeArrayIndex(final String[] args, final int index) {
        final List<String> argsList = Lists.newArrayList(args);
        argsList.remove(index);
        return argsList.toArray(new String[0]);
    }

    public boolean isNumeric(final String string) {
        return string.matches("[0-9]+");
    }

    @Override
    public boolean isMaintenance() {
        return settings.isMaintenance();
    }

    @Override
    public boolean isTaskRunning() {
        return runnable != null;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public IEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }

    public List<String> getMaintenanceServersDump() {
        return isMaintenance() ? Arrays.asList("global") : null;
    }

    public int getSaltLevel() {
        return Integer.MAX_VALUE;
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

    public MaintenanceCommand getCommandManager() {
        return commandManager;
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

    public abstract Task startMaintenanceRunnable(Runnable runnable);

    public abstract SenderInfo getOfflinePlayer(String name);

    public abstract SenderInfo getOfflinePlayer(UUID uuid);

    public abstract File getDataFolder();

    public abstract InputStream getResource(String name);

    public abstract Logger getLogger();

    public abstract String getServerVersion();

    public abstract List<PluginDump> getPlugins();

    protected abstract void loadIcon(File file) throws Exception;

    protected abstract void kickPlayers();

    protected abstract File getPluginFile();
}
