package com.sweet.redis;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class RedisCache implements Cache{
    private String id;
    private JedisPool redisClient=createRedis();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); //读写锁

    private final static RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();



    public void setReadWriteLock(ReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
    }

    public RedisCache(String id) {
        if(id==null){
            throw new IllegalArgumentException("Cache instance requires an ID");
        }
        log.debug("create an cache instance with id"+id);
        this.id=id;
    }

    public String getId() {
        return this.id;
    }

    /**从连接池中取
     * @return
     */
    private static JedisPool createRedis() {//从连接池获取redis连接
        return RedisPool.getPool();
    }

    public void putObject(Object key, Object value) {
        try (Jedis jedis = this.redisClient.getResource();){
            jedis.set(serializer.serialize(key), serializer.serialize(value));
        }
    }

    public Object getObject(Object key) {
        //缓存穿透
        try (Jedis jedis = this.redisClient.getResource();){
            byte[] values=jedis.get(serializer.serialize(key));
            if(values==null){
                return null;
            }
            return serializer.deserialize(values);
        }
    }

    public Object removeObject(Object key) {
        try (Jedis jedis = this.redisClient.getResource();){
            return  jedis.expire(serializer.serialize(key), 0);
        }
    }

    public void clear() {
        try (Jedis jedis = this.redisClient.getResource();){
            Set<byte[]> keys = jedis.keys(("*"+id+"*").getBytes());//匹配当前mapper下所有的缓存
            for (byte[] key : keys) {
                jedis.del(key);
            }
            //jedis.flushDB();
        }
    }

    /*private Object execute(RedisCallback callback) {
        try (Jedis jedis = this.redisClient.getResource();){
            return callback.doWithRedis(jedis);
        }
    }*/

    /*public void clear() {
        execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                jedis.del(id);
                return null;
            }
        });
    }*/

    public int getSize() {
        try (Jedis jedis = this.redisClient.getResource();){
            Long size = jedis.dbSize();
            return Integer.valueOf(size+"");
        }
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

}