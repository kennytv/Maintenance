package eu.kennytv.maintenance.api;

public interface IMaintenance {

    /**
     * Enabled/disabled maintenance mode.
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
     * @return true if maintenance is enabled on the proxy
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
     * @return {@link ISettings} for more options
     */
    ISettings getSettings();
}
