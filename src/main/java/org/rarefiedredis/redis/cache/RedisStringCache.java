package org.rarefiedredis.redis.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache key-value pairs as strings.
 */
public final class RedisStringCache implements IRedisCache<String, String> {

    /**
     * Holds the actual cache.
     */
    private Map<String, String> cache;

    /**
     * Constructor. Initializes an empty cache.
     */
    public RedisStringCache() {
        cache = new HashMap<>();
    }

    @Override
    public boolean exists(final String key) {
        return cache.containsKey(key);
    }

    @Override
    public void remove(final String key) {
        cache.remove(key);
    }

    @Override
    public void set(final String key, final String value, final Object... arguments) {
        cache.put(key, value);
    }

    @Override
    public String get(final String key) {
        return cache.get(key);
    }

    @Override
    public boolean removeValue(final String key, final String value) {
        if (!exists(key)) {
            return false;
        }
        if (cache.get(key).equals(value)) {
            remove(key);
            return true;
        }
        return false;
    }

    @Override
    public String type() {
        return "string";
    }

}
