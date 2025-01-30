/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
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

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.api.Maintenance;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.event.MaintenanceChangedEvent;
import eu.kennytv.maintenance.api.event.manager.EventManager;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.dump.MaintenanceDump;
import eu.kennytv.maintenance.core.dump.PluginDump;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.runnable.MaintenanceScheduleRunnable;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Task;
import eu.kennytv.maintenance.core.util.Version;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.format.NamedTextColor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public abstract class MaintenancePlugin implements Maintenance {
    public static final Gson GSON = new GsonBuilder().create();
    public static final String HANGAR_URL = "https://hangar.papermc.io/kennytv/Maintenance";
    private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
    protected final EventManager eventManager;
    protected final Version version;
    protected Settings settings;
    protected ServerListPlusHook serverListPlusHook;
    protected MaintenanceRunnable runnable;
    protected MaintenanceCommand commandManager;
    private final Component prefix;
    private final ServerType serverType;
    private Version newestVersion;
    private boolean debug;

    protected MaintenancePlugin(final String version, final ServerType serverType) {
        this.version = new Version(version);
        this.serverType = serverType;
        this.prefix = Component.text()
                .append(Component.text().content("[").color(NamedTextColor.DARK_GRAY))
                .append(Component.text().content("Maintenance").color(NamedTextColor.YELLOW))
                .append(Component.text().content("]").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(" "))
                .build();
        this.eventManager = new eu.kennytv.maintenance.core.event.EventManager();
        MaintenanceProvider.setMaintenance(this);
    }

    public void disable() {
    }

    @Override
    public void setMaintenance(final boolean maintenance) {
        settings.setMaintenance(maintenance);
        settings.getConfig().set("maintenance-enabled", maintenance);
        settings.saveConfig();
        serverActions(maintenance);

        for (final String command : (maintenance ? settings.getCommandsOnMaintenanceEnable() : settings.getCommandsOnMaintenanceDisable())) {
            try {
                executeConsoleCommand(command);
            } catch (final Exception e) {
                getLogger().log(Level.SEVERE, "Error while executing extra maintenance " + (maintenance ? "enable" : "disable") + " command: " + command, e);
            }
        }
    }

    public void serverActions(final boolean maintenance) {
        if (isTaskRunning()) {
            cancelTask();
        }
        if (serverListPlusHook != null && settings.isEnablePingMessages()) {
            serverListPlusHook.setEnabled(!maintenance);
        }

        if (maintenance) {
            broadcast(settings.getMessage("maintenanceActivated"));
            if (settings.isKickOnlinePlayers()) {
                kickPlayers();
            }
        } else {
            broadcast(settings.getMessage("maintenanceDeactivated"));
        }

        eventManager.callEvent(new MaintenanceChangedEvent(maintenance));
    }

    public String replacePingVariables(String component) {
        if (component.contains("%TIMER%")) {
            component = component.replace("%TIMER%", getTimerMessage());
        }
        component = component.replace("%ONLINE%", String.valueOf(getOnlinePlayers()));
        component = component.replace("%MAX%", String.valueOf(getMaxPlayers()));
        return component;
    }

    public String getTimerMessage() {
        if (!isTaskRunning()) {
            return settings.getLanguageString("motdTimerNotRunning");
        }

        final int preHours = runnable.getSecondsLeft() / 60;
        final int minutes = preHours % 60;
        final int seconds = runnable.getSecondsLeft() % 60;
        return settings.getLanguageString("motdTimer",
                "%HOURS%", String.format("%02d", preHours / 60),
                "%MINUTES%", String.format("%02d", minutes),
                "%SECONDS%", String.format("%02d", seconds));
    }

    public String getFormattedTime(final int timeSeconds) {
        final int preHours = timeSeconds / 60;
        final int minutes = preHours % 60;
        final int seconds = timeSeconds % 60;

        final StringBuilder buider = new StringBuilder();
        append(buider, "hour", preHours / 60);
        append(buider, "minute", minutes);
        append(buider, "second", seconds);
        return buider.toString();
    }

    private void append(final StringBuilder builder, final String timeUnit, final int time) {
        if (time == 0) return;
        if (builder.length() != 0) {
            builder.append(' ');
        }
        builder.append(time).append(' ').append(settings.language.getString(time == 1 ? timeUnit : timeUnit + "s"));
    }

    public void startMaintenanceRunnable(final Duration duration, final boolean enable) {
        runnable = new MaintenanceRunnable(this, settings, (int) duration.getSeconds(), enable);
        // Save the endtimer to be able to continue it after a server stop
        if (settings.isSaveEndtimerOnStop() && !runnable.shouldEnable()) {
            settings.setSavedEndtimer(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(runnable.getSecondsLeft()));
        }
    }

    public void scheduleMaintenanceRunnable(final Duration enableIn, final Duration maintenanceDuration) {
        runnable = new MaintenanceScheduleRunnable(this, settings, (int) enableIn.getSeconds(), (int) maintenanceDuration.getSeconds());
    }

    public boolean updateAvailable() {
        try {
            checkNewestVersion();
            return version.compareTo(newestVersion) < 0;
        } catch (final Exception e) {
            return false;
        }
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
            startMaintenanceRunnable(Duration.ofMillis(settings.getSavedEndtimer() - current), false);
            getLogger().info("The timer has been continued - maintenance will be disabled in: " + getTimerMessage());
        }
    }

    protected void sendEnableMessage() {
        if (!settings.hasUpdateChecks()) return;
        async(() -> {
            try {
                checkNewestVersion();
            } catch (final Exception e) {
                return;
            }

            final int compare = version.compareTo(newestVersion);
            if (compare < 0) {
                getLogger().warning("Newest version available: Version " + newestVersion + ", you're on " + version);
            } else if (compare > 0) {
                if (version.getTag().equalsIgnoreCase("snapshot")) {
                    getLogger().info("You're running a development version, please report bugs on the Discord server (https://discord.gg/vGCUzHq) or the GitHub issue tracker (https://github.com/kennytv/Maintenance/issues)");
                } else {
                    getLogger().info("You're running a version, that doesn't exist!");
                }
            }
        });
    }

    public boolean installUpdate() throws Exception {
        // Sponge and Velocity need their own jar
        final String platformInfix = serverType == ServerType.VELOCITY ? "Velocity-" : serverType == ServerType.SPONGE ? "Sponge-" : "";
        final String fileName = "Maintenance-" + platformInfix + newestVersion + ".jar";
        final Path tempFilePath = Paths.get(getPluginFolder() + "Maintenance.tmp");
        final URLConnection connection = new URL("https://github.com/kennytv/Maintenance/releases/download/" + newestVersion + "/" + fileName).openConnection();
        try (final BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
             final BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(tempFilePath))) {
            writeFile(is, os);
        }

        final File file = tempFilePath.toFile();
        final long newlength = file.length();
        if (newlength < 10_000) {
            // Sanity check the file length
            file.delete();
            return false;
        }

        try (final InputStream is = Files.newInputStream(file.toPath());
             final BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(getPluginFile().toPath()))) {
            writeFile(is, os);
        }

        file.delete();
        return true;
    }

    private void writeFile(final InputStream is, final OutputStream os) throws IOException {
        final byte[] chunk = new byte[1024];
        int chunkSize;
        while ((chunkSize = is.read(chunk)) != -1) {
            os.write(chunk, 0, chunkSize);
        }
    }

    private void checkNewestVersion() throws Exception {
        final URLConnection connection = new URL("https://hangar.papermc.io/api/v1/projects/Maintenance/latestrelease").openConnection();
        connection.setRequestProperty("User-Agent", "Maintenance/" + getVersion());
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            final String newVersionString = reader.readLine();
            final Version newVersion = new Version(newVersionString);
            if (!newVersion.equals(version)) {
                newestVersion = newVersion;
            }
        }
    }

    public String pasteDump() {
        final MaintenanceDump dump = new MaintenanceDump(this, settings);
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://api.pastes.dev/post").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Maintenance/" + getVersion());
            connection.setRequestProperty("Content-Type", "text/plain");

            try (final OutputStream out = connection.getOutputStream()) {
                out.write(new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(dump).getBytes(StandardCharsets.UTF_8));
            }

            if (connection.getResponseCode() == 503) {
                getLogger().warning("Could not paste dump, pastes.dev down?");
                return null;
            }

            try (final InputStream in = connection.getInputStream()) {
                final String output = CharStreams.toString(new InputStreamReader(in));
                final JsonObject jsonOutput = GSON.fromJson(output, JsonObject.class);
                if (!jsonOutput.has("key")) {
                    getLogger().log(Level.WARNING, "Could not paste dump, there was no key returned :(");
                    return null;
                }

                return jsonOutput.get("key").getAsString();
            }
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
            getLogger().log(Level.WARNING, "Could not load the 'maintenance-icon.png' file!");
            e.printStackTrace();
        }
    }

    public void cancelTask() {
        if (settings.isSaveEndtimerOnStop() && !runnable.shouldEnable()) {
            settings.setSavedEndtimer(0);
        }

        runnable.getTask().cancel();
        runnable = null;
    }

    @Nullable
    public UUID checkUUID(final SenderInfo sender, final String s) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(s);
        } catch (final Exception e) {
            sender.send(settings.getMessage("invalidUuid"));
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
        return INT_PATTERN.matcher(string).matches();
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
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }

    @Nullable
    public List<String> getMaintenanceServersDump() {
        return isMaintenance() ? Arrays.asList("global") : null;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public int getSaltLevel() {
        return Integer.MAX_VALUE;
    }

    public Version getNewestVersion() {
        return newestVersion;
    }

    public Component prefix() {
        return prefix;
    }

    /**
     * @see #isTaskRunning()
     */
    @Nullable
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

    protected abstract void executeConsoleCommand(String command);

    public abstract void broadcast(Component component);

    public abstract Task startMaintenanceRunnable(Runnable runnable);

    /**
     * Gets the offline sender info of a player.
     * This method may do a web lookup.
     *
     * @param name name of the player
     */
    public abstract CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(String name);

    public abstract CompletableFuture<@Nullable SenderInfo> getOfflinePlayer(UUID uuid);

    public abstract File getDataFolder();

    @Nullable
    public InputStream getResource(final String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    public abstract Logger getLogger();

    public abstract String getServerVersion();

    public abstract List<PluginDump> getPlugins();

    protected abstract void loadIcon(File file) throws Exception;

    protected abstract void kickPlayers();

    protected abstract File getPluginFile();

    protected abstract int getOnlinePlayers();

    protected abstract int getMaxPlayers();

    public abstract void addWhitelist(UUID uuid, String player);

    public abstract void removeWhitelist(UUID uuid);
}
