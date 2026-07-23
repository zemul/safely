package com.anbao.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtils {

	private static JedisPool pool = null;

	static {
		InputStream in = JedisPoolUtils.class.getClassLoader().getResourceAsStream("redis.properties");
		Properties pro = new Properties();
		try {
			pro.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(Integer.parseInt(pro.get("redis.maxIdle").toString()));
		poolConfig.setMinIdle(Integer.parseInt(pro.get("redis.minIdle").toString()));
		poolConfig.setMaxTotal(Integer.parseInt(pro.get("redis.maxTotal").toString()));

		String host = pro.getProperty("redis.url", "127.0.0.1");
		int port = Integer.parseInt(pro.getProperty("redis.port", "6379"));
		String password = pro.getProperty("redis.password");

		if (password != null && !password.trim().isEmpty()) {
			pool = new JedisPool(poolConfig, host, port, 2000, password);
		} else {
			pool = new JedisPool(poolConfig, host, port);
		}
	}

	public static Jedis getJedis() {
		return pool.getResource();
	}
}
