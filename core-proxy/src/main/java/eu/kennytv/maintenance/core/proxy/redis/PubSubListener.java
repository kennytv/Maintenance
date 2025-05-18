package eu.kennytv.maintenance.core.proxy.redis;

import io.lettuce.core.pubsub.RedisPubSubListener;

public abstract class PubSubListener<K, V> implements RedisPubSubListener<K, V> {
    @Override
    public abstract void message(K redisChannel, V givenObject);

    @Override
    public void message(K k, K k1, V v) {

    }

    @Override
    public void subscribed(K k, long l) {

    }

    @Override
    public void psubscribed(K k, long l) {

    }

    @Override
    public void unsubscribed(K k, long l) {

    }

    @Override
    public void punsubscribed(K k, long l) {

    }

    public abstract String redisChannel();
}