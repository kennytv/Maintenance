package eu.kennytv.maintenance.core.proxy.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.resource.ClientResources;

import java.util.Collections;
import java.util.List;

public class RedisHandler {
    private RedisPubSubAsyncCommands<String, Object> commandsPublisher;
    private RedisPubSubAsyncCommands<String, Object> commandsReceiver;

    private StatefulRedisConnection<String, String> connection;
    private RedisReactiveCommands<String, String> reactiveCommands;

    private RedisClient redisClient;

    public RedisHandler(String uri) {
        redisClient =  RedisClient.create(
                ClientResources.builder()
                    .ioThreadPoolSize(2)
                    .build(),
                RedisURI.create(uri)
        );

        connection = redisClient.connect();
        reactiveCommands = connection.reactive();
        connection.async();

        this.commandsPublisher = redisClient.connectPubSub(new RedisSerializer()).async();
        this.commandsReceiver = redisClient.connectPubSub(new RedisSerializer()).async();
    }

    public String get(String key) {
        try (StatefulRedisConnection<String, String> connection = getConnection()) {
            return connection.sync().get(key);
        } catch (Exception e) {
            System.out.println("Error while getting redis key: " + key);
            e.printStackTrace();
            return null;
        }
    }

    public Long delete(String key) {
        try (StatefulRedisConnection<String, String> connection = getConnection()) {
            return connection.sync().del(key);
        } catch (Exception e) {
            System.out.println("Errore durante il remove redis key: " + key);
            e.printStackTrace();
            return null;
        }
    }

    public String set(String key, String value) {
        try (StatefulRedisConnection<String, String> connection = getConnection()) {
            return connection.sync().set(key, value);
        } catch (Exception e) {
            System.out.println("Error while getting redis key: " + key);
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getList(String key) {
        try (StatefulRedisConnection<String, String> connection = getConnection()) {
            return connection.sync().lrange(key, 0, -1);
        } catch (Exception e) {
            System.out.println("Error while getting list from Redis, key: " + key);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void setList(String key, List<String> values) {
        try {
            StatefulRedisConnection<String, String> connection = getConnection();

            // Controlla se la connessione è chiusa e riaprila se necessario
            if (connection == null || !connection.isOpen()) {
                connection = reconnect(); // Assicurati di avere un metodo per riaprire la connessione
            }

            RedisCommands<String, String> commands = connection.sync();
            commands.del(key); // Elimina la lista esistente
            if (!values.isEmpty()) {
                commands.rpush(key, values.toArray(new String[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(RedisPacket packet){
        sendPacket(packet.redisChannel(), packet);
    }

    public void sendPacket(String channel, Object object){
        commandsPublisher.publish(channel, object);
    }

    public void registerReceiver(PubSubListener<String, Object> listener){
        commandsReceiver.getStatefulConnection().addListener(listener);
        commandsReceiver.subscribe(listener.redisChannel());
    }

    public void close() {
        commandsPublisher.getStatefulConnection().closeAsync(); // close the publisher
        commandsReceiver.getStatefulConnection().closeAsync();
        connection.close();// close the receiver
    }

    public synchronized StatefulRedisConnection<String, String> reconnect() {
        if (connection != null && connection.isOpen()) {
            return connection;
        }

        try {
            connection = redisClient.connect();
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public StatefulRedisConnection<String, String> getConnection() {
        if (connection == null || !connection.isOpen()) {
            connection = redisClient.connect();
        }
        return connection;
    }
}