package org.rarefiedredis.redis.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cache key-value pairs as a set.
 */
public final class RedisSetCache implements IRedisCache<String, Set<String>> {

    /**
     * Holds the actual cache.
     */
    private Map<String, Set<String>> cache;

    /**
     * Constructor. Initializes an empty cache.
     */
    public RedisSetCache() {
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
        if (!cache.containsKey(key)) {
            cache.put(key, new HashSet<>());
        }
        cache.get(key).add(value);
    }

    @Override
    public Set<String> get(final String key) {
        return cache.get(key);
    }

    @Override
    public boolean removeValue(final String key, final String value) {
        return exists(key) && cache.get(key).remove(value);
    }

    @Override
    public String type() {
        return "set";
    }

}
