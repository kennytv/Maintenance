package eu.kennytv.maintenance.api.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author KennyTV
 * @since 2.5
 */
public interface IMaintenanceBungee extends IMaintenance {

    /**
     * Enables/disables maintenance mode on a proxied server.
     * If enabled, all non-permitted players will be kicked.
     * If MySQL is enabled, it will also be written into the database.
     *
     * @param maintenance true to enable, false to disable maintenance mode
     */
    boolean setMaintenanceToServer(ServerInfo server, boolean maintenance);

    /**
     * @return true if maintenance is currently enabled on the proxied server
     */
    boolean isMaintenance(ServerInfo server);

    /**
     * @return true if a start- or endtimer task is currently running regarding the proxied server
     */
    boolean isServerTaskRunning(ServerInfo server);
}
