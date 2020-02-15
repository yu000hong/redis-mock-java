package org.rarefiedredis.redis.cache;

import java.util.HashMap;
import java.util.Map;

public final class RedisHashCache implements IRedisCache<String, Map<String, String>> {

    private Map<String, Map<String, String>> cache;

    public RedisHashCache() {
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
    public void set(String key, String field, Object... arguments) {
        String value = (String)arguments[0];
        if (!cache.containsKey(key)) {
            cache.put(key, new HashMap<>());
        }
        cache.get(key).put(field, value);
    }

    @Override
    public Map<String, String> get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean removeValue(String key, String field) {
        if (!exists(key)) {
            return false;
        }
        if (cache.get(key).containsKey(field)) {
            cache.get(key).remove(field);
            return true;
        }
        return false;
    }

    @Override
    public String type() {
        return "hash";
    }

}
