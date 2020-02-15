package org.rarefiedredis.redis.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class RedisListCache implements IRedisCache<String, List<String>> {

    private Map<String, List<String>> cache;

    public RedisListCache() {
        cache = new HashMap<>();
    }

    @Override
    public boolean exists(String key) {
        return cache.containsKey(key);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void set(String key, String value, Object... arguments) {
        if (!cache.containsKey(key)) {
            cache.put(key, new LinkedList<>());
        }
        if (arguments.length == 1) {
            cache.get(key).add((Integer) arguments[0], value);
        } else {
            cache.get(key).add(value);
        }
    }

    @Override
    public List<String> get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean removeValue(String key, String value) {
        return exists(key) && cache.get(key).remove(value);
    }

    @Override
    public String type() {
        return "list";
    }

}
