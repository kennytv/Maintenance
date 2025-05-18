package eu.kennytv.maintenance.core.proxy.redis.impl;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.redis.MaintenanceRedisPacket;

import java.io.Serial;

public class MaintenanceAddServerPacket extends MaintenanceRedisPacket {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String serverName;

    public MaintenanceAddServerPacket(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void execute() {
        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
        assert api != null;
        Server server = api.getServer(serverName);
        api.setMaintenanceToServer(server, true);
    }
}
