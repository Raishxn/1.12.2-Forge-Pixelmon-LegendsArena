package com.envyful.api.database.impl;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.type.RedisDatabaseDetails;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.redis.Subscribe;
import com.google.common.collect.Lists;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class SimpleJedisDatabase implements Database {

    private final JedisPool pool;
    private final List<ReflectivePubSub> pubSubs = Lists.newArrayList();

    public SimpleJedisDatabase(RedisDatabaseDetails details) {
        this(details.getIp(), details.getPort(), details.getPassword());
    }

    public SimpleJedisDatabase(String host, int port, String password) {
        this.pool = this.initJedis(host, port, password);
    }

    private JedisPool initJedis(String ip, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(256);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return new JedisPool(poolConfig, ip, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    @Override
    public Jedis getJedis() throws JedisException, UnsupportedOperationException {
        return this.pool.getResource();
    }

    @Override
    public void close() {
        this.pubSubs.forEach(JedisPubSub::unsubscribe);
        this.pool.close();
    }

    @Override
    public Connection getConnection() throws SQLException, UnsupportedOperationException {
        throw new UnsupportedOperationException("CANNOT CREATE SQL CONNECTION FROM JEDIS");
    }

    @Override
    public void subscribe(Object o) throws JedisException, UnsupportedOperationException {
        for (Method declaredMethod : o.getClass().getDeclaredMethods()) {
            Subscribe subscribe = declaredMethod.getAnnotation(Subscribe.class);

            if (subscribe == null) {
                continue;
            }

            UtilConcurrency.runAsync(() -> {
                try (Jedis jedis = this.getJedis()) {
                    ReflectivePubSub sub = new ReflectivePubSub(o, declaredMethod);

                    this.pubSubs.add(sub);
                    jedis.subscribe(sub, subscribe.value());
                } catch (JedisException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static final class ReflectivePubSub extends JedisPubSub {

        private final Object o;
        private final Method method;

        private ReflectivePubSub(Object o, Method method) {
            this.o = o;
            this.method = method;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                this.method.invoke(o, channel, message);
            } catch (InvocationTargetException | IllegalAccessException e) {
                System.out.println("Got bad jedis in channel: '" + channel + "'. Message being: '" + message + "'");
                e.printStackTrace();
            }
        }
    }
}
