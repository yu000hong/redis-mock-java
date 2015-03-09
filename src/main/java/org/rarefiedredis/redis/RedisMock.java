import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public final class RedisMock extends AbstractRedisMock {

    private RedisStringCache stringCache;
    private RedisListCache listCache;
    private RedisSetCache setCache;
    private RedisSortedSetCache zsetCache;
    private RedisHashCache hashCache;
    private List<IRedisCache> caches;
    private Map<String, Timer> timers;

    public RedisMock() {
        stringCache = new RedisStringCache();
        listCache = new RedisListCache();
        setCache = new RedisSetCache();
        zsetCache = new RedisSortedSetCache();
        hashCache = new RedisHashCache();
        caches = new ArrayList<IRedisCache>();
        caches.add(stringCache);
        caches.add(listCache);
        caches.add(setCache);
        caches.add(zsetCache);
        caches.add(hashCache);
        timers = new HashMap<String, Timer>();
    }

    @Override public synchronized Integer del(String ... keys) {
        int deleted = 0;
        String key;
        for (int idx = 0; idx < keys.length; idx += 1) {
            key = keys[idx];
            for (IRedisCache cache : caches) {
                if (cache.exists(key)) {
                    cache.remove(key);
                    deleted += 1;
                    break;
                }
            }
        }
        return deleted;
    }

    @Override public synchronized Integer exists(String key) {
        for (IRedisCache cache : caches) {
            if (cache.exists(key)) {
                return 1;
            }
        }
        return 0;
    }

    @Override public synchronized Integer expire(String key, long seconds) {
        return this.pexpire(key, seconds*1000);
    }

    @Override public synchronized Integer expireat(String key, long timestamp) {
        Date now = new Date();
        return pexpire(key, timestamp*1000 - now.getTime());
    }

    @Override public synchronized Integer persist(String key) {
        if (exists(key) == 1 && timers.containsKey(key)) {
            timers.get(key).cancel();
            timers.remove(key);
            return 1;
        }
        return 0;
    }

    @Override public synchronized Integer pexpire(final String key, long milliseconds) {
        if (exists(key) == 1) {
            Timer timer = new Timer();
            timers.put(key, timer);
            timer.schedule(new TimerTask() {
                    @Override public void run() {
                        for (IRedisCache cache : caches) {
                            if (cache.exists(key)) {
                                caches.remove(key);
                                return;
                            }
                        }
                    }
                }, milliseconds);
            return 1;
        }
        return 0;
    }

    @Override public synchronized Integer pexpireat(String key, long timestamp) {
        Date now = new Date();
        return this.pexpire(key, timestamp - now.getTime());
    }

    @Override public synchronized String type(String key) {
        for (IRedisCache cache : caches) {
            if (cache.exists(key)) {
                return cache.type();
            }
        }
        return "none";
    }

}
