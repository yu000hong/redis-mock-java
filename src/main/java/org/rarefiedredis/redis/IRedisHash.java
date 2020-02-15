package org.rarefiedredis.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRedisHash {

    Long hdel(String key, String field, String... fields);

    Boolean hexists(String key, String field);

    String hget(String key, String field);

    Map<String, String> hgetall(String key);

    Long hincrby(String key, String field, long increment);

    String hincrbyfloat(String key, String field, double increment);

    Set<String> hkeys(String key);

    Long hlen(String key);

    List<String> hmget(String key, String field, String... fields);

    void hmset(String key, String field, String value, String... fieldAndValues);

    Boolean hset(String key, String field, String value);

    Boolean hsetnx(String key, String field, String value);

    Long hstrlen(String key, String field);

    List<String> hvals(String key);

    ScanResult<Map<String, String>> hscan(String key, long cursor, String... options);

}
