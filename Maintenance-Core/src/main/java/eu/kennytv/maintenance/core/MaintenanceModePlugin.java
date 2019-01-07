package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
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

    protected MaintenanceModePlugin(final String prefix, final String version, final ServerType serverType) {
        this.prefix = prefix;
        this.version = new Version(version);
        this.serverType = serverType;
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
                return "§cYou're running a development version, please report bugs on the Discord server (https://kennytv.eu/discord) or the GitHub tracker (https://kennytv.eu/maintenance/issues)";
            } else {
                return "§cYou're running a version, that doesn't exist! §cN§ai§dc§ee§5!";
            }
        }
        return "You have the latest version of the plugin installed.";
    }

    public void checkNewestVersion() {
        try {
            final HttpURLConnection c = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=40699").openConnection();
            final String newVersionString = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
            final Version newVersion = new Version(newVersionString);
            if (!newVersion.equals(version))
                newestVersion = newVersion;
        } catch (final Exception ignored) {
        }
    }

    public boolean installUpdate() {
        try {
            final URLConnection conn = new URL("https://github.com/KennyTV/Maintenance/releases/download/" + newestVersion + "/Maintenance.jar").openConnection();
            final InputStream is = new BufferedInputStream(conn.getInputStream());
            final OutputStream os = new BufferedOutputStream(new FileOutputStream("plugins/Maintenance.tmp"));
            final byte[] chunk = new byte[1024];
            int chunkSize;
            while ((chunkSize = is.read(chunk)) != -1) {
                os.write(chunk, 0, chunkSize);
            }
            os.close();
            final File newfile = new File("plugins/Maintenance.tmp");
            final long newlength = newfile.length();
            if (newlength <= 10000) {
                newfile.delete();
                return false;
            }

            final FileInputStream is2 = new FileInputStream(new File("plugins/Maintenance.tmp"));
            final OutputStream os2 = new BufferedOutputStream(new FileOutputStream(getPluginFile()));
            final byte[] chunk2 = new byte[1024];
            int chunkSize2;
            while ((chunkSize2 = is2.read(chunk2)) != -1)
                os2.write(chunk2, 0, chunkSize2);
            is2.close();
            os2.close();
            //TODO nicht jedes mal file objekt neu nehmen?
            new File("plugins/Maintenance.tmp").delete();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public abstract File getPluginFile();

    public abstract Logger getLogger();
}
