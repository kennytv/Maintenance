package eu.kennytv.maintenance.core.proxy.redis.impl;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.core.proxy.redis.MaintenanceRedisPacket;

import java.util.UUID;

public class MaintenanceRemoveWhitelistPacket extends MaintenanceRedisPacket {

    private final UUID uuid;

    public MaintenanceRemoveWhitelistPacket(UUID uuid) {
        this.uuid = uuid;
    }


    @Override
    public void execute() {
        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
        api.getSettings().removeWhitelistedPlayer(uuid);
    }
}
