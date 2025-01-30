package eu.kennytv.maintenance.core.proxy.redis.impl;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.core.proxy.redis.MaintenanceRedisPacket;

import java.util.UUID;

public class MaintenanceAddWhitelistPacket extends MaintenanceRedisPacket {

    private final UUID uuid;
    private final String player;

    public MaintenanceAddWhitelistPacket(UUID uuid, String player) {
        this.uuid = uuid;
        this.player = player;
    }


    @Override
    public void execute() {
        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
        api.getSettings().addWhitelistedPlayer(uuid, player);
    }
}
