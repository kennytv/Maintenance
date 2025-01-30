package eu.kennytv.maintenance.core.proxy.redis;

import eu.kennytv.maintenance.core.proxy.SettingsProxy;

import java.io.Serializable;


public abstract class MaintenanceRedisPacket implements RedisPacket, Serializable {

    @Override
    public String redisChannel() {
        return SettingsProxy.REDIS_CHANNEL;
    }
}