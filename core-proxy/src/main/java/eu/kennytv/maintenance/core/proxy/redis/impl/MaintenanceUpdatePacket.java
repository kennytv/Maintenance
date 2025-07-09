package eu.kennytv.maintenance.core.proxy.redis.impl;


import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.core.proxy.redis.MaintenanceRedisPacket;

import java.io.Serial;

public class MaintenanceUpdatePacket extends MaintenanceRedisPacket {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean proxyMaintenance;

    public MaintenanceUpdatePacket(boolean proxyMaintenance) {
        this.proxyMaintenance = proxyMaintenance;
    }

    @Override
    public void execute() {
        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
        assert api != null;
        api.setMaintenance(proxyMaintenance);
    }
}