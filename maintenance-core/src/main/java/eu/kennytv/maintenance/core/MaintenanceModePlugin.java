package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.core.hook.ServerListPlusHook;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.ServerType;
import eu.kennytv.maintenance.core.util.Version;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    @Override
    public boolean isMaintenance() {
        return getSettings().isMaintenance();
    }

    @Override
    public String getVersion() {
        return version.toString();
    }

    @Override
    public boolean isTaskRunning() {
        return taskId != 0 && runnable != null;
    }

    public void startMaintenanceRunnable(final int minutes, final boolean enable) {
        runnable = new MaintenanceRunnable(this, (Settings) getSettings(), minutes, enable);
        taskId = startMaintenanceRunnable(runnable);
    }

    public Version getNewestVersion() {
        return newestVersion;
    }

    protected abstract int startMaintenanceRunnable(Runnable runnable);

    public abstract void async(Runnable runnable);

    public abstract void cancelTask();

    public abstract File getPluginFile();

    public abstract void broadcast(String message);

    public MaintenanceRunnable getRunnable() {
        return runnable;
    }

    public String getPrefix() {
        return prefix;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public String formatedTimer() {
        if (!isTaskRunning()) return "-";
        final int preHours = runnable.getSecondsLeft() / 60;
        final int minutes = preHours % 60;
        final int seconds = runnable.getSecondsLeft() % 60;
        return String.format("%02d:%02d:%02d", preHours / 60, minutes, seconds);
    }

    public void checkNewestVersion() {
        try {
            final HttpURLConnection c = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=40699").openConnection();
            final String newVersionString = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();

            final Version newVersion = new Version(newVersionString);
            final boolean available = !newVersion.equals(version);
            if (available)
                newestVersion = newVersion;
        } catch (final Exception ignored) {
        }
    }

    public boolean updateAvailable() {
        checkNewestVersion();
        return version.compareTo(newestVersion) == -1;
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

    public boolean installUpdate() {
        try {
            URL url = null;
            try {
                url = new URL("https://github.com/KennyTV/Maintenance/releases/download/" + newestVersion + "/Maintenance.jar");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }

            final URLConnection conn = url.openConnection();
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
            } else {
                final FileInputStream is2 = new FileInputStream(new File("plugins/Maintenance.tmp"));
                final OutputStream os2 = new BufferedOutputStream(new FileOutputStream(getPluginFile()));
                final byte[] chunk2 = new byte[1024];
                int chunkSize2;
                while ((chunkSize2 = is2.read(chunk2)) != -1)
                    os2.write(chunk2, 0, chunkSize2);
                is2.close();
                os2.close();

                final File tmp = new File("plugins/Maintenance.tmp");
                tmp.delete();
                return true;
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract Logger getLogger();
}
