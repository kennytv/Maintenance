package eu.kennytv.maintenance.core.proxy.redis;


public class RedisPacketReceiver extends PubSubListener<String, Object> {

    private final String redisChannel;

    public RedisPacketReceiver(String redisChannel) {
        this.redisChannel = redisChannel;
    }

    @Override
    public void message(String redisChannel, Object givenObject) {
        if (redisChannel.equals(this.redisChannel())) {
            if (givenObject instanceof RedisPacket packet) packet.execute();
        }
    }

    @Override
    public String redisChannel() {
        return redisChannel;
    }
}