package com.xiebiao.tools.redisfx.model;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Bill Xie
 * @since 2025/8/10 09:58
 **/
public record RedisConnectionInfo(
        String host,
        int port,
        String password,
        String username,
        String connectionName) {
    public RedisConnectionInfo {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host cannot be null or empty");
        }
        if (username.isEmpty()) {
            username = "default";
        }
        if (connectionName.isEmpty()) {
            connectionName = host + "@" + port;
        }
        if (username.isEmpty()) {
            username = "default";
        }
    }

}
