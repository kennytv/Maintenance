package eu.kennytv.maintenance.core.proxy.redis;

import java.io.Serializable;

public abstract class MaintenanceRedisPacket implements RedisPacket, Serializable {

    @Override
    public String redisChannel() {
        return "MUTILITY:ALL";
    }
}