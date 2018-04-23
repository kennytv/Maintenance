package eu.kennytv.maintenance.api;

/**
 * @author KennyTV
 * @since 2.1
 */
public interface IMaintenance {

    /**
     * Enables/disables maintenance mode.
     * If enabled, all non-permitted players will be kicked.
     * <p>
     * If using the BungeeCord version and MySQL is enabled,
     * it will also be written into the database.
     * </p>
     *
     * @param maintenance true to enable, false to disable maintenance mode
     */
    void setMaintenance(boolean maintenance);

    /**
     * @return true if maintenance is currently enabled
     */
    boolean isMaintenance();

    /**
     * @return true if a timer task is currently running
     */
    boolean isTaskRunning();

    /**
     * @return version of the plugin
     */
    String getVersion();

    /**
     * @return {@link ISettings} for options regarding configuration files
     */
    ISettings getSettings();
}
