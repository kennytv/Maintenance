package eu.kennytv.maintenance.core.proxy.redis;

public interface RedisPacket {

    void execute();
    String redisChannel();

}