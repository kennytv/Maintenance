package eu.kennytv.maintenance.api;

/**
 * @author KennyTV
 * @since 2.2.2
 */
public interface IMaintenanceBase {

    /**
     * This method is used internally when getting the api instance.
     *
     * @return {@link IMaintenance} instance
     */
    IMaintenance getApi();
}
