package org.rarefiedredis.redis;

import org.rarefiedredis.redis.cache.IRedisCache;
import org.rarefiedredis.redis.cache.RedisHashCache;
import org.rarefiedredis.redis.cache.RedisListCache;
import org.rarefiedredis.redis.cache.RedisSetCache;
import org.rarefiedredis.redis.cache.RedisSortedSetCache;
import org.rarefiedredis.redis.cache.RedisStringCache;
import org.rarefiedredis.redis.exception.ArgException;
import org.rarefiedredis.redis.exception.BitArgException;
import org.rarefiedredis.redis.exception.DiscardWithoutMultiException;
import org.rarefiedredis.redis.exception.ExecWithoutMultiException;
import org.rarefiedredis.redis.exception.IndexOutOfRangeException;
import org.rarefiedredis.redis.exception.NoKeyException;
import org.rarefiedredis.redis.exception.NotFloatException;
import org.rarefiedredis.redis.exception.NotFloatHashException;
import org.rarefiedredis.redis.exception.NotFloatMinMaxException;
import org.rarefiedredis.redis.exception.NotIntegerException;
import org.rarefiedredis.redis.exception.NotIntegerHashException;
import org.rarefiedredis.redis.exception.NotValidStringRangeItemException;
import org.rarefiedredis.redis.exception.SyntaxErrorException;
import org.rarefiedredis.redis.exception.WrongTypeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An in-memory redis-compatible key-value cache and store written
 * in pure Java.
 */
public final class RedisMock extends AbstractRedisMock {

    private final class WatchKey {
        public Boolean modified;
        public List<Integer> watchers;

        public WatchKey() {
            this.modified = false;
            this.watchers = new ArrayList<>();
        }
    }

    /**
     * Cache to hold strings.
     */
    private RedisStringCache stringCache;
    /**
     * Cache to hold lists.
     */
    private RedisListCache listCache;
    /**
     * Cache to hold sets.
     */
    private RedisSetCache setCache;
    /**
     * Cache to hold hashes.
     */
    private RedisHashCache hashCache;
    /**
     * Cache to hold sorted sets.
     */
    private RedisSortedSetCache zsetCache;
    /**
     * List of all our caches.
     */
    private List<IRedisCache> caches;
    /**
     * Expiration timers.
     */
    private Map<String, Timer> timers;
    /**
     * Expirations.
     */
    private Map<String, Long> expirations;
    /**
     * Watchers.
     */
    private Map<String, WatchKey> watchers;

    /**
     * Default constructor. Initializes an empty redis
     * database.
     */
    public RedisMock() {
        stringCache = new RedisStringCache();
        listCache = new RedisListCache();
        setCache = new RedisSetCache();
        hashCache = new RedisHashCache();
        zsetCache = new RedisSortedSetCache();
        caches = new ArrayList<>();
        caches.add(stringCache);
        caches.add(listCache);
        caches.add(setCache);
        caches.add(hashCache);
        caches.add(zsetCache);
        timers = new HashMap<>();
        expirations = new HashMap<>();
        watchers = new HashMap<>();
    }

    /**
     * Always throws a CloneNotSupportedException. Cloning RedisMock
     * instances is not supported.
     *
     * @return Nothing, since this function always throws an exception.
     * @throws CloneNotSupportedException Always
     */
    @Override
    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public IRedisClient createClient() {
        return new RedisMockClient(this);
    }

    private void checkType(String key, String type) throws WrongTypeException {
        if (exists(key) && !type(key).equals(type)) {
            throw new WrongTypeException();
        }
    }

    private void keyModified(String key) {
        if (watchers.containsKey(key)) {
            watchers.get(key).modified = true;
        }
    }

    //region IRedisKeys implementations

    @Override
    public synchronized Long del(final String... keys) {
        long deleted = 0L;
        String key;
        for (int idx = 0; idx < keys.length; idx += 1) {
            key = keys[idx];
            timers.remove(key);
            expirations.remove(key);
            for (IRedisCache cache : caches) {
                if (cache.exists(key)) {
                    cache.remove(key);
                    keyModified(key);
                    deleted += 1L;
                    break;
                }
            }
        }
        return deleted;
    }

    @Override
    public synchronized Boolean exists(final String key) {
        for (IRedisCache cache : caches) {
            if (cache.exists(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Boolean expire(final String key, final int seconds) {
        return this.pexpire(key, seconds * 1000);
    }

    @Override
    public synchronized Boolean expireat(final String key, final long timestamp) {
        Date now = new Date();
        return pexpire(key, timestamp * 1000 - now.getTime());
    }

    @Override
    public synchronized Boolean persist(final String key) {
        if (exists(key) && timers.containsKey(key)) {
            timers.get(key).cancel();
            timers.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public synchronized Boolean pexpire(final String key, final long milliseconds) {
        if (exists(key)) {
            Timer timer = new Timer();
            timers.put(key, timer);
            expirations.put(key, System.currentTimeMillis() + milliseconds);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    del(key);
                }
            }, milliseconds);
            return true;
        }
        return false;
    }

    @Override
    public synchronized Boolean pexpireat(final String key, final long timestamp) {
        Date now = new Date();
        return this.pexpire(key, timestamp - now.getTime());
    }

    @Override
    public synchronized Long ttl(final String key) {
        Long ms = pttl(key);
        if (ms < 0L) {
            return ms;
        }
        return ms / 1000L;
    }

    @Override
    public synchronized Long pttl(final String key) {
        if (!exists(key)) {
            return -2L;
        }
        if (!timers.containsKey(key)) {
            return -1L;
        }
        return expirations.get(key) - System.currentTimeMillis();
    }

    @Override
    public synchronized String type(final String key) {
        for (IRedisCache cache : caches) {
            if (cache.exists(key)) {
                return cache.type();
            }
        }
        return "none";
    }

    //endregion

    //region IRedisString implementations

    @Override
    public synchronized Long append(final String key, final String value) {
        checkType(key, "string");
        if (!exists(key)) {
            set(key, value);
        } else {
            stringCache.set(key, stringCache.get(key) + value);
        }
        keyModified(key);
        return strlen(key);
    }

    @Override
    public synchronized Long bitcount(final String key, long... options) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "string");
        String str = stringCache.get(key);
        long len = str.length();
        long start = options.length > 0 ? options[0] : 0L;
        long end = options.length > 1 ? options[1] : len - 1;
        if (end >= len) {
            end = len - 1;
        }
        if (start < 0) {
            start = len + start;
        }
        if (end < 0) {
            end = len + end;
        }
        if (start > end) {
            return 0L;
        }
        long count = 0;
        // TODO: Slow bit-counting, do map to do fast bit counting;
        for (long idx = start; idx <= end; ++idx) {
            int n = Character.codePointAt(str, (int) idx);
            while (n != 0) {
                count += (n & 1);
                n >>= 1;
            }
        }
        return count;
    }

    @Override
    public synchronized Long bitop(String operation, final String destKey, String... keys) {
        int len = keys.length;
        if (len == 0) {
            return 0L;
        }
        String[] strings = new String[len];
        int longest = 0;
        for (int i = 0; i < len; ++i) {
            String key = keys[i];
            if (!exists(key)) {
                strings[i] = "";
                continue;
            }
            checkType(key, "string");
            strings[i] = stringCache.get(key);
            if (longest < strings[i].length()) {
                longest = strings[i].length();
            }
        }
        for (int i = 0; i < len; ++i) {
            while (strings[i].length() < longest) {
                strings[i] += "\0";
            }
        }
        operation = operation.toLowerCase();
        String s0 = strings[0];
        for (int i = 0; i < len; ++i) {
            String si = strings[i];
            StringBuilder cur = new StringBuilder();
            for (int j = 0; j < longest; ++j) {
                int n = 0;
                switch (operation) {
                    case "and":
                        n = Character.codePointAt(s0, j) & Character.codePointAt(si, j);
                        break;
                    case "or":
                        n = Character.codePointAt(s0, j) | Character.codePointAt(si, j);
                        break;
                    case "xor":
                        // a XOR a = 0, so avoid XOR'ing the first string with itself.
                        if (i > 0) {
                            n = Character.codePointAt(s0, j) ^ Character.codePointAt(si, j);
                        } else {
                            n = Character.codePointAt(s0, j);
                        }
                        break;
                    case "not":
                        n = ~Character.codePointAt(s0, j);
                        break;
                }
                cur.append((char) n);
            }
            s0 = cur.toString();
            if (operation.equals("not")) {
                break;
            }
        }
        set(destKey, s0);
        return (long) s0.length();
    }

    @Override
    public synchronized Long bitpos(String key, long bit, long... options) {
        if (bit != 0L && bit != 1L) {
            throw new BitArgException();
        }
        checkType(key, "string");
        if (!exists(key)) {
            if (bit == 0L) {
                return 0L;
            }
            return -1L;
        }
        String value = stringCache.get(key);
        long len = (long) value.length();
        long start = options.length > 0 ? options[0] : 0;
        long end = options.length > 1 ? options[1] : len - 1;
        boolean noEnd = !(options.length > 1);
        if (start < 0) {
            start = len + start;
        }
        if (end < 0) {
            end = len + start;
        }
        if (start > end) {
            return -1L;
        }
        long i;
        for (i = start; i <= end; ++i) {
            int ch = Character.codePointAt(value, (int) i);
            int cnt = 0;
            while (cnt < 16) {
                if (bit == 0L && (ch & 0x8000) != 0x8000) {
                    return i * 16L + (long) cnt;
                }
                if (bit == 1L && (ch & 0x8000) == 0x8000) {
                    return i * 16L + (long) cnt;
                }
                ch <<= 1;
                cnt += 1;
            }
        }
        if (bit == 1) {
            return -1L;
        } else if (noEnd) {
            return i * 16L;
        } else {
            return -1L;
        }
    }

    @Override
    public synchronized Long decr(String key) {
        return decrby(key, 1);
    }

    @Override
    public synchronized Long decrby(String key, long decrement) {
        checkType(key, "string");
        long oldValue = 0L;
        if (exists(key)) {
            //noinspection ConstantConditions
            oldValue = Long.parseLong(get(key));
        }
        long newValue = oldValue - decrement;
        set(key, String.valueOf(newValue));
        return newValue;
    }

    @Override
    public synchronized String get(final String key) {
        if (!exists(key)) {
            return null;
        }
        checkType(key, "string");
        return stringCache.get(key);
    }

    @Override
    public synchronized Boolean getbit(final String key, final long offset) {
        if (!exists(key)) {
            return false;
        }
        checkType(key, "string");
        String value = stringCache.get(key);
        if (offset >= value.length() * 16L) {
            return false;
        }
        int n = value.codePointAt((int) Math.floor(offset / 16L));
        long pos = offset % 16;
        return ((n >> pos) & 0x01) == 1;
    }

    @Override
    public synchronized String getrange(final String key, long start, long end) {
        if (!exists(key)) {
            return "";
        }
        checkType(key, "string");
        String value = stringCache.get(key);
        if (end < 0) {
            end = value.length() + end;
        }
        if (start < 0) {
            start = value.length() + start;
        }
        try {
            return value.substring((int) start, (int) (end + 1L));
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public synchronized String getset(final String key, final String value) {
        String oldValue = get(key);
        set(key, value);
        return oldValue;
    }

    @Override
    public synchronized Long incr(final String key) {
        return decrby(key, -1);
    }

    @Override
    public synchronized Long incrby(final String key, final long increment) {
        return decrby(key, -increment);
    }

    @Override
    public synchronized String incrbyfloat(final String key, final double increment) {
        checkType(key, "string");
        Double oldValue = 0.0d;
        if (exists(key)) {
            //noinspection ConstantConditions
            oldValue = Double.parseDouble(get(key));
        }
        double newValue = oldValue + increment;
        set(key, String.valueOf(newValue));
        return String.valueOf(newValue);
    }

    @Override
    public synchronized String[] mget(final String... keys) {
        int len = keys.length;
        String[] gets = new String[len];
        for (int i = 0; i < len; ++i) {
            gets[i] = get(keys[i]);
        }
        return gets;
    }

    @Override
    public synchronized String mset(final String... keyAndValues) {
        if (keyAndValues.length == 0 || keyAndValues.length % 2 != 0) {
            throw new ArgException("mset");
        }
        for (int idx = 0; idx < keyAndValues.length; ++idx) {
            if (idx % 2 != 0) {
                continue;
            }
            set(keyAndValues[idx], keyAndValues[idx + 1]);
        }
        return "OK";
    }

    @Override
    public synchronized Boolean msetnx(final String... keyAndValues) {
        if (keyAndValues.length == 0 || keyAndValues.length % 2 != 0) {
            throw new ArgException("msetnx");
        }
        for (int idx = 0; idx < keyAndValues.length; ++idx) {
            if (idx % 2 != 0) {
                continue;
            }
            if (exists(keyAndValues[idx])) {
                return false;
            }
        }
        for (int idx = 0; idx < keyAndValues.length; ++idx) {
            if (idx % 2 != 0) {
                continue;
            }
            try {
                set(keyAndValues[idx], keyAndValues[idx + 1]);
            } catch (SyntaxErrorException e) {
            }
        }
        return true;
    }

    @Override
    public synchronized String psetex(String key, long milliseconds, String value) {
        try {
            set(key, value, "px", String.valueOf(milliseconds));
        } catch (SyntaxErrorException e) {
        }
        return "OK";
    }

    @Override
    public synchronized String set(final String key, final String value, String... options) {
        boolean nx = false, xx = false;
        int ex = -1;
        long px = -1;
        for (Object option : options) {

        }
        for (int idx = 0; idx < options.length; ++idx) {
            String option = options[idx];
            if (option == "nx") {
                nx = true;
            } else if (option == "xx") {
                xx = true;
            } else if (option == "ex") {
                if (idx + 1 >= options.length) {
                    throw new SyntaxErrorException();
                }
                ex = Integer.parseInt(options[idx + 1]);
            } else if (option == "px") {
                if (idx + 1 >= options.length) {
                    throw new SyntaxErrorException();
                }
                px = Long.parseLong(options[idx + 1]);
            }
        }
        if (nx) {
            if (exists(key)) {
                return null;
            }
        }
        if (xx) {
            if (!exists(key)) {
                return null;
            }
            del(key);
        }
        if (!nx && !xx) {
            if (exists(key)) {
                del(key);
            }
        }
        stringCache.set(key, value);
        keyModified(key);
        if (ex != -1) {
            expire(key, ex);
        }
        if (px != -1) {
            pexpire(key, px);
        }
        return "OK";
    }

    @Override
    public synchronized Long setbit(final String key, final long offset, final boolean value) {
        checkType(key, "string");
        if (!exists(key)) {
            set(key, "");
        }
        int byteIdx = (int) Math.floor(offset / 16L);
        int bitIdx = (int) (offset % 16L);
        String val = get(key);
        while (val.length() < byteIdx + 1) {
            val += "\0";
        }
        int code = val.codePointAt(byteIdx);
        int idx = 0;
        int mask = 0x8000;
        while (idx < bitIdx) {
            mask >>= 1;
            idx += 1;
        }
        int bit = (code & mask) == 0 ? 0 : 1;
        if (!value) {
            code = code & (~mask);
        } else {
            code = code | mask;
        }
        String newVal = "";
        newVal += val.substring(0, byteIdx);
        newVal += (char) (code);
        newVal += val.substring(byteIdx + 1);
        set(key, newVal);
        return (long) bit;
    }

    @Override
    public synchronized String setex(final String key, final int seconds, final String value) {
        set(key, value, "ex", String.valueOf(seconds));
        return "OK";
    }

    @Override
    public synchronized Long setnx(final String key, final String value) {
        if (!exists(key)) {
            set(key, value);
            return 1L;
        }
        return 0L;
    }

    @Override
    public synchronized Long setrange(final String key, final long offset, final String value) {
        checkType(key, "string");
        if (!exists(key)) {
            set(key, "");
        }
        String val = get(key);
        int idx;
        for (idx = val.length(); idx < (int) (offset + value.length()); ++idx) {
            val += "\0";
        }
        String newValue = val.substring(0, (int) offset);
        for (idx = (int) offset; idx < (int) (offset + value.length()); ++idx) {
            newValue += value.charAt(idx - (int) offset);
        }
        newValue += val.substring((int) (offset + value.length()));
        try {
            set(key, newValue);
        } catch (SyntaxErrorException e) {
        }
        return (long) newValue.length();
    }

    @Override
    public synchronized Long strlen(final String key) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "string");
        return (long) stringCache.get(key).length();
    }

    @Override
    public List<Long> bitfield(String key, String... options) {
        checkType(key, "string");
        List<BitCommand> commands = BitCommand.parse(options);
        long maxLen = 0;
        for (BitCommand command : commands) {
            if (command.getActualOffset() + command.type.size > maxLen) {
                maxLen = command.getActualOffset() + command.type.size;
            }
        }
        String value;
        if (exists(key)) {
            value = stringCache.get(key);
        } else {
            value = "";
        }
        long len = (maxLen - 1) / 16 + 1;
        while (value.length() < len) {
            value += "\0";
        }
        stringCache.set(key, value);
        byte[] bytes = getBytes(value);
        List<Long> result = new ArrayList<>(commands.size());
        boolean modified = false;
        for (BitCommand command : commands) {
            result.add(command.exec(bytes));
            if (!"get".equals(command.command)) {
                modified = true;
            }
        }
        if (modified) {
            stringCache.set(key, toString(bytes));
            keyModified(key);
        }
        return result;
    }

    //endregion

    //region IRedisList implementations

    @Override
    public synchronized String lindex(final String key, long index) {
        if (!exists(key)) {
            return null;
        }
        checkType(key, "list");
        if (index < 0) {
            index = listCache.get(key).size() + index;
        }
        try {
            return listCache.get(key).get((int) index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public synchronized Long linsert(final String key, String beforeOrAfter, final String pivot, final String value) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "list");
        int index = listCache.get(key).indexOf(pivot);
        beforeOrAfter = beforeOrAfter.toLowerCase();
        if (index != -1) {
            if (beforeOrAfter.equals("before")) {
                listCache.set(key, value, index);
                keyModified(key);
            } else if (beforeOrAfter.equals("after")) {
                listCache.set(key, value, index + 1);
                keyModified(key);
            }
            return llen(key);
        }
        return -1L;
    }

    @Override
    public synchronized Long llen(final String key) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "list");
        List<String> lst = listCache.get(key);
        Long len = 0L;
        int size = lst.size();
        len += (long) size;
        if (size == Integer.MAX_VALUE) {
            //TODO why??
            // Hm, we may have _more_ elements, so count the rest.
            for (String elem : lst) {
                len += 1;
            }
        }
        return len;
    }

    @Override
    public synchronized String lpop(final String key) {
        if (!exists(key)) {
            return null;
        }
        checkType(key, "list");
        try {
            String popped = listCache.get(key).remove(0);
            if (listCache.get(key).isEmpty()) {
                del(key);
            }
            keyModified(key);
            return popped;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public synchronized Long lpush(final String key, final String element, final String... elements) {
        checkType(key, "list");
        listCache.set(key, element, 0);
        for (String elem : elements) {
            listCache.set(key, elem, 0);
        }
        keyModified(key);
        return llen(key);
    }

    @Override
    public synchronized Long lpushx(final String key, final String element) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "list");
        return lpush(key, element);
    }

    @Override
    public synchronized List<String> lrange(final String key, long start, long stop) {
        if (!exists(key)) {
            return new ArrayList<>();
        }
        checkType(key, "list");
        List<String> list = listCache.get(key);
        int len = list.size();
        if (start < 0) {
            start = len + start;
        }
        if (stop < 0) {
            stop = len + stop;
        }
        if (start > stop) {
            return new ArrayList<>();
        }
        if (start > list.size() - 1) {
            return new ArrayList<>();
        }
        if (stop > len - 1) {
            stop = len - 1;
        }
        if (start < 0 || stop < 0) {
            return new ArrayList<>();
        }
        return list.subList((int) start, (int) (stop + 1L));
    }

    @Override
    public synchronized Long lrem(final String key, final long count, final String element) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "list");
        long cnt = 0L;
        while (listCache.get(key).remove(element)) {
            cnt += 1;
            if (count > 0 && cnt == count) {
                break;
            }
        }
        if (listCache.get(key).size() == 0) {
            del(key);
        }
        if (cnt > 0L) {
            keyModified(key);
        }
        return cnt;
    }

    @Override
    public synchronized String lset(final String key, final long index, final String element) {
        if (!exists(key)) {
            throw new NoKeyException();
        }
        checkType(key, "list");
        if (index >= listCache.get(key).size()) {
            throw new IndexOutOfRangeException();
        }
        listCache.get(key).set((int) index, element);
        keyModified(key);
        return "OK";
    }

    @Override
    public synchronized String ltrim(final String key, long start, long stop) {
        if (!exists(key)) {
            return "OK";
        }
        checkType(key, "list");
        int len = listCache.get(key).size();
        if (start > len || start > stop) {
            del(key);
            return "OK";
        }
        if (start < 0) {
            start = len + start;
        }
        if (stop < 0) {
            stop = len + start;
        }
        if (stop > len - 1) {
            stop = len - 1;
        }
        if (start < 0 || stop < 0) {
            return "OK";
        }
        List<String> subList = listCache.get(key).subList((int) start, (int) (stop + 1L));
        // Avoid a ConcurrentModificationException on the subList view by copying it into a new list.
        List<String> trimmed = new LinkedList<>();
        trimmed.addAll(subList);
        listCache.get(key).retainAll(trimmed);
        keyModified(key);
        return "OK";
    }

    @Override
    public synchronized String rpop(final String key) {
        if (!exists(key)) {
            return null;
        }
        checkType(key, "list");
        try {
            String popped = listCache.get(key).remove(listCache.get(key).size() - 1);
            if (listCache.get(key).isEmpty()) {
                del(key);
            }
            keyModified(key);
            return popped;
        } catch (IndexOutOfBoundsException ie) {
            return null;
        }
    }

    @Override
    public synchronized String rpoplpush(final String source, final String dest) {
        if (!exists(source)) {
            return null;
        }
        checkType(source, "list");
        checkType(dest, "list");
        String element = rpop(source);
        lpush(dest, element);
        return element;
    }

    @Override
    public synchronized Long rpush(final String key, final String element, final String... elements) {
        checkType(key, "list");
        listCache.set(key, element);
        for (String elem : elements) {
            listCache.set(key, elem);
        }
        keyModified(key);
        return llen(key);
    }

    @Override
    public synchronized Long rpushx(final String key, final String element) {
        if (!exists(key)) {
            return 0L;
        }
        checkType(key, "list");
        return rpush(key, element);
    }

    //endregion

    //region IRedisSet implementations

    @Override
    public synchronized Long sadd(final String key, final String member, final String... members) {
        checkType(key, "set");
        Long count = 0L;
        if (!setCache.exists(key) || !setCache.get(key).contains(member)) {
            setCache.set(key, member);
            count += 1L;
        }
        for (String m : members) {
            if (!setCache.get(key).contains(m)) {
                setCache.set(key, m);
                count += 1L;
            }
        }
        if (count > 0L) {
            keyModified(key);
        }
        return count;
    }

    @Override
    public synchronized Long scard(String key) {
        checkType(key, "set");
        if (!setCache.exists(key)) {
            return 0L;
        }
        return (long) setCache.get(key).size();
    }

    @Override
    public synchronized Set<String> sdiff(String key, String... keys) {
        checkType(key, "set");
        for (String k : keys) {
            checkType(k, "set");
        }
        Set<String> diff = new HashSet<>(smembers(key));
        for (String k : keys) {
            diff.removeAll(smembers(k));
        }
        return diff;
    }

    @Override
    public synchronized Long sdiffstore(String destination, String key, String... keys) throws WrongTypeException {
        Set<String> diff = sdiff(key, keys);
        if (exists(destination)) {
            del(destination);
        }
        for (String d : diff) {
            sadd(destination, d);
        }
        keyModified(destination);
        return (long) diff.size();
    }

    @Override
    public synchronized Set<String> sinter(String key, String... keys) {
        checkType(key, "set");
        for (String k : keys) {
            checkType(k, "set");
        }
        Set<String> inter = new HashSet<>(smembers(key));
        for (String k : keys) {
            inter.retainAll(smembers(k));
        }
        return inter;
    }

    @Override
    public synchronized Long sinterstore(String destination, String key, String... keys) {
        Set<String> inter = sinter(key, keys);
        if (exists(destination)) {
            del(destination);
        }
        for (String i : inter) {
            sadd(destination, i);
        }
        keyModified(destination);
        return (long) inter.size();
    }

    @Override
    public synchronized Boolean sismember(String key, String member) {
        checkType(key, "set");
        return setCache.exists(key) && setCache.get(key).contains(member);
    }

    @Override
    public synchronized Set<String> smembers(String key) {
        checkType(key, "set");
        if (!exists(key)) {
            return Collections.unmodifiableSet(new HashSet<String>());
        }
        return Collections.unmodifiableSet(setCache.get(key));
    }

    @Override
    public synchronized Boolean smove(String source, String dest, String member) {
        checkType(source, "set");
        checkType(dest, "set");
        Long rem = srem(source, member);
        if (rem == 0L) {
            return false;
        }
        sadd(dest, member);
        keyModified(source);
        keyModified(dest);
        return rem == 1L;
    }

    @Override
    public synchronized Set<String> spop(String key, long count) {
        Set<String> members = srandmember(key, count);
        if (!members.isEmpty()) {
            for (String member : members) {
                setCache.removeValue(key, member);
            }
            keyModified(key);
        }
        return members;
    }

    @Override
    public synchronized Set<String> srandmember(String key, long count) {
        boolean negative = (count < 0);
        count = Math.abs(count);
        Set<String> set = new HashSet<>((int) count);
        while (set.size() < (int) count) {
            for (String member : setCache.get(key)) {
                set.add(member);
                if (set.size() == (int) count) {
                    break;
                }
            }
            if (!negative) {
                break;
            }
        }
        return set;
    }

    @Override
    public synchronized Long srem(String key, String member, String... members) {
        checkType(key, "set");
        if (!setCache.exists(key)) {
            return 0L;
        }
        Long count = 0L;
        if (setCache.removeValue(key, member)) {
            count += 1L;
        }
        for (String m : members) {
            if (setCache.removeValue(key, m)) {
                count += 1L;
            }
        }
        if (count > 0L) {
            keyModified(key);
        }
        return count;
    }

    @Override
    public synchronized Set<String> sunion(String key, String... keys) {
        checkType(key, "set");
        for (String k : keys) {
            checkType(k, "set");
        }
        Set<String> union = new HashSet<>(smembers(key));
        for (String k : keys) {
            union.addAll(smembers(k));
        }
        return union;
    }

    @Override
    public synchronized Long sunionstore(String destination, String key, String... keys) {
        Set<String> union = sunion(key, keys);
        if (exists(destination)) {
            del(destination);
        }
        for (String u : union) {
            sadd(destination, u);
        }
        keyModified(destination);
        return (long) union.size();
    }

    @Override
    public synchronized ScanResult<Set<String>> sscan(String key, long cursor, String... options) {
        checkType(key, "set");
        MatchAndCount matchAndCount = MatchAndCount.parse(options);
        Set<String> scanned = new HashSet<>();
        Set<String> members = smembers(key);
        Long idx = 0L;
        for (String member : members) {
            idx += 1;
            if (idx > cursor) {
                if (matchAndCount.matches(member)) {
                    scanned.add(member);
                }
                if ((long) scanned.size() >= matchAndCount.count) {
                    break;
                }
            }
        }
        if (idx >= scard(key)) {
            idx = 0L;
        }
        return new ScanResult<>(idx, scanned);
    }

    //endregion

    //region IRedisHash implementations

    @Override
    public synchronized Long hdel(String key, String field, String... fields) {
        checkType(key, "hash");
        if (!exists(key)) {
            return 0L;
        }
        Long count = 0L;
        if (hashCache.removeValue(key, field)) {
            count += 1L;
        }
        for (String f : fields) {
            if (hashCache.removeValue(key, f)) {
                count += 1L;
            }
        }
        if (count > 0L) {
            keyModified(key);
        }
        return count;
    }

    @Override
    public synchronized Boolean hexists(String key, String field) {
        checkType(key, "hash");
        return exists(key) && hashCache.get(key).containsKey(field);
    }

    @Override
    public synchronized String hget(String key, String field) {
        checkType(key, "hash");
        if (!exists(key)) {
            return null;
        }
        return hashCache.get(key).get(field);
    }

    @Override
    public synchronized Map<String, String> hgetall(String key) {
        checkType(key, "hash");
        if (!exists(key)) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(hashCache.get(key));
    }

    @Override
    public synchronized Long hincrby(String key, String field, long increment) {
        checkType(key, "hash");
        if (!hexists(key, field)) {
            hset(key, field, String.valueOf(increment));
        } else {
            try {
                @SuppressWarnings("ConstantConditions")
                Long no = Long.valueOf(hget(key, field));
                hset(key, field, String.valueOf(no + increment));
            } catch (NumberFormatException nfe) {
                throw new NotIntegerHashException();
            }
        }
        keyModified(key);
        //noinspection ConstantConditions
        return Long.valueOf(hget(key, field));
    }

    @Override
    public synchronized String hincrbyfloat(String key, String field, double increment) {
        checkType(key, "hash");
        if (!hexists(key, field)) {
            hset(key, field, String.valueOf(increment));
        } else {
            try {
                @SuppressWarnings("ConstantConditions")
                Double no = Double.parseDouble(hget(key, field));
                hset(key, field, String.valueOf(no + increment));
            } catch (NumberFormatException nfe) {
                throw new NotFloatHashException();
            }
        }
        keyModified(key);
        return hget(key, field);
    }

    @Override
    public synchronized Set<String> hkeys(String key) {
        checkType(key, "hash");
        if (!exists(key)) {
            return new HashSet<>();
        }
        return hashCache.get(key).keySet();
    }

    @Override
    public synchronized Long hlen(String key) {
        checkType(key, "hash");
        if (!exists(key)) {
            return 0L;
        }
        return (long) hashCache.get(key).size();
    }

    @Override
    public synchronized List<String> hmget(String key, String field, String... fields) {
        checkType(key, "hash");
        List<String> lst = new ArrayList<>(1 + fields.length);
        if (!exists(key)) {
            for (int idx = 0; idx < 1 + fields.length; ++idx) {
                lst.add(null);
            }
            return lst;
        }
        lst.add(hget(key, field));
        for (String f : fields) {
            lst.add(hget(key, f));
        }
        return lst;
    }

    @Override
    public synchronized void hmset(String key, String field, String value, String... fieldAndValues) {
        checkType(key, "hash");
        if (fieldAndValues.length % 2 != 0) {
            throw new ArgException("HMSET");
        }
        hashCache.set(key, field, value);
        for (int idx = 0; idx < fieldAndValues.length; ++idx) {
            if (idx % 2 != 0) {
                continue;
            }
            hashCache.set(key, fieldAndValues[idx], fieldAndValues[idx + 1]);
        }
        keyModified(key);
    }

    @Override
    public synchronized Boolean hset(String key, String field, String value) {
        checkType(key, "hash");
        boolean ret = true;
        if (exists(key) && hashCache.get(key).containsKey(field)) {
            ret = false;
        }
        hashCache.set(key, field, value);
        keyModified(key);
        return ret;
    }

    @Override
    public synchronized Boolean hsetnx(String key, String field, String value) {
        checkType(key, "hash");
        boolean ret = false;
        if (!exists(key) || !hashCache.get(key).containsKey(field)) {
            ret = true;
            hset(key, field, value);
        }
        return ret;
    }

    @Override
    public synchronized Long hstrlen(String key, String field) {
        checkType(key, "hash");
        if (!exists(key) || !hashCache.get(key).containsKey(field)) {
            return 0L;
        }
        return (long) hashCache.get(key).get(field).length();
    }

    @Override
    public synchronized List<String> hvals(String key) {
        checkType(key, "hash");
        if (!exists(key)) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(new ArrayList<>(hashCache.get(key).values()));
    }

    @Override
    public synchronized ScanResult<Map<String, String>> hscan(String key, long cursor, String... options) {
        checkType(key, "hash");
        MatchAndCount matchAndCount = MatchAndCount.parse(options);
        Map<String, String> scanned = new HashMap<>();
        Map<String, String> all = hgetall(key);
        Long idx = 0L;
        for (String k : all.keySet()) {
            idx += 1;
            if (idx > cursor) {
                if (matchAndCount.matches(k)) {
                    scanned.put(k, all.get(k));
                }
                if ((long) scanned.size() >= matchAndCount.count) {
                    break;
                }
            }
        }
        return new ScanResult<>(idx, scanned);
    }

    //endregion

    //region IRedisTransaction commands

    @Override
    public String discard() throws DiscardWithoutMultiException {
        throw new DiscardWithoutMultiException();
    }

    @Override
    public List<Object> exec() throws ExecWithoutMultiException {
        throw new ExecWithoutMultiException();
    }

    @Override
    public IRedisClient multi() {
        return new RedisMockMulti(this);
    }

    @Override
    public synchronized String unwatch() {
        return unwatch(this.hashCode());
    }

    public synchronized String unwatch(Integer hashCode) {
        List<String> keysToRemove = new LinkedList<>();
        for (String key : watchers.keySet()) {
            watchers.get(key).watchers.remove(hashCode);
            if (watchers.get(key).watchers.size() == 0) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            watchers.remove(key);
        }
        return "OK";
    }

    @Override
    public synchronized String watch(String key) {
        return watch(key, this.hashCode());
    }

    public synchronized String watch(String key, Integer hashCode) {
        if (!watchers.containsKey(key)) {
            watchers.put(key, new WatchKey());
        }
        watchers.get(key).watchers.add(hashCode);
        return "OK";
    }

    @Override
    public synchronized boolean modified(Integer hashCode, String command, List<Object> args) {
        List<String> keys = new LinkedList<>();
        if (args.get(0) instanceof String[]) {
            if (command.equals("mset") || command.equals("msetnx")) {
                String[] keysvalues = (String[]) args.get(0);
                for (int idx = 0; idx < keysvalues.length; ++idx) {
                    if (idx % 2 == 0) {
                        keys.add(keysvalues[idx]);
                    }
                }
            } else {
                for (String key : (String[]) args.get(0)) {
                    keys.add(key);
                }
            }
        } else {
            if (command.equals("bitop")) {
                keys.add((String) args.get(1));
            } else {
                keys.add((String) args.get(0));
            }
        }
        if (command.equals("rpoplpush")) {
            keys.add((String) args.get(1));
        }
        if (command.equals("sdiff") || command.equals("sinter") || command.equals("sunion")) {
            for (String k : (String[]) args.get(1)) {
                keys.add(k);
            }
        }
        if (command.equals("sdiffstore") || command.equals("sinterstore") || command.equals("sunionstore")) {
            keys.add((String) args.get(1));
            for (String k : (String[]) args.get(2)) {
                keys.add(k);
            }
        }
        if (command.equals("smove")) {
            keys.add((String) args.get(1));
        }
        for (String key : keys) {
            if (watchers.containsKey(key) && watchers.get(key).modified && watchers.get(key).watchers.contains(hashCode)) {
                return true;
            }
        }
        // TODO: Multi-key commands.
        return false;
    }

    //endregion

    //region IRedisSortedSet commands

    @Override
    public synchronized Long zadd(final String key, final ZsetPair pair, final ZsetPair... pairs) {
        checkType(key, "zset");
        Long count = 0L;
        if (!zsetCache.exists(key) || !zsetCache.get(key).contains(pair.member)) {
            ++count;
        }
        zsetCache.set(key, pair.member, pair.score);
        for (ZsetPair p : pairs) {
            if (p == null) {
                continue;
            }
            if (!zsetCache.get(key).contains(p.member)) {
                ++count;
            }
            zsetCache.set(key, p.member, p.score);
        }
        keyModified(key);
        return count;
    }

    @Override
    public Long zadd(final String key, final double score, final String member, final Object... scoreAndMembers) {
        if (scoreAndMembers.length % 2 != 0) {
            throw new SyntaxErrorException();
        }
        ZsetPair pair = new ZsetPair(score, member);
        ZsetPair[] pairs = new ZsetPair[scoreAndMembers.length / 2];
        for (int idx = 0, pidx = 0; idx < scoreAndMembers.length; ++idx) {
            if (idx % 2 != 0) {
                continue;
            }
            if (scoreAndMembers[idx] instanceof Number) {
                scoreAndMembers[idx] = ((Number) scoreAndMembers[idx]).doubleValue();
            }
            if (!(scoreAndMembers[idx] instanceof Double)) {
                throw new NotFloatException();
            }
            if (!(scoreAndMembers[idx + 1] instanceof String)) {
                scoreAndMembers[idx + 1] = scoreAndMembers[idx + 1].toString();
            }
            pairs[pidx] = new ZsetPair((Double) scoreAndMembers[idx], (String) scoreAndMembers[idx + 1]);
            ++pidx;
        }
        return zadd(key, pair, pairs);
    }

    @Override
    public synchronized Long zcard(final String key) {
        checkType(key, "zset");
        if (!zsetCache.exists(key)) {
            return 0L;
        }
        return (long) zsetCache.get(key).size();
    }

    @Override
    public synchronized Long zcount(final String key, final double min, final double max) {
        checkType(key, "zset");
        if (!zsetCache.exists(key)) {
            return 0L;
        }
        Long count = 0L;
        for (String member : zsetCache.get(key)) {
            Double score = zsetCache.getScore(key, member);
            //noinspection ConstantConditions
            if (min <= score && score <= max) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public synchronized String zincrby(final String key, final double increment, final String member) {
        checkType(key, "zset");
        Double newScore = increment;
        if (zsetCache.existsValue(key, member)) {
            newScore += zsetCache.getScore(key, member);
        }
        zsetCache.set(key, member, newScore);
        keyModified(key);
        return String.valueOf(newScore);
    }

    @Override
    public synchronized Long zinterstore(final String destination, final int numkeys, final String... options) {
        if (exists(destination)) {
            del(destination);
        }
        ZsetAggregation zsetAggregation = ZsetAggregation.parse(numkeys, options);
        zsetAggregation.keys.forEach(key -> checkType(key, "zset"));
        String key = zsetAggregation.keys.get(0);
        Set<ZsetPair> range = new HashSet<>(zrange(key, 0, -1, "withscores"));
        for (ZsetPair pair : range) {
            if (zsetAggregation.weights.containsKey(key)) {
                pair.score *= zsetAggregation.weights.get(key);
            }
        }
        for (String k : zsetAggregation.keys.subList(1, numkeys)) {
            Set<ZsetPair> inter = new HashSet<>();
            for (ZsetPair pair : range) {
                if (!zsetCache.existsValue(k, pair.member)) {
                    continue;
                }
                Double score = zsetCache.getScore(k, pair.member);
                if (zsetAggregation.weights.containsKey(k)) {
                    score *= zsetAggregation.weights.get(k);
                }
                pair.score = zsetAggregation.aggregation.aggregate(pair.score, score);
                inter.add(pair);
            }
            range = inter;
        }
        Long count = 0L;
        for (ZsetPair pair : range) {
            zadd(destination, pair);
            ++count;
        }
        keyModified(destination);
        return count;
    }

    @Override
    public synchronized Long zlexcount(final String key, String min, String max) {
        return (long) zrangebylex(key, min, max).size();
    }

    private List<ZsetPair> zpop(String key, long count, boolean max) {
        checkType(key, "zset");
        List<ZsetPair> list = new ArrayList<>((int) count);
        if (!exists(key)) {
            return list;
        }
        SortedSet<String> zset = (SortedSet<String>) zsetCache.get(key);
        for (int i = 0; i < count; i++) {
            try {
                String member = max ? zset.last() : zset.first();
                Double score = zsetCache.getScore(key, member);
                list.add(new ZsetPair(member, score));
                zsetCache.removeValue(key, member);
            } catch (NoSuchElementException ignored) {
                break;
            }
        }
        if (!list.isEmpty()) {
            keyModified(key);
        }
        return list;
    }

    @Override
    public List<ZsetPair> zpopmax(String key, long count) {
        return zpop(key, count, true);
    }

    @Override
    public List<ZsetPair> zpopmin(String key, long count) {
        return zpop(key, count, true);
    }

    @Override
    public synchronized List<ZsetPair> zrange(final String key, long start, long stop, final String... options) {
        checkType(key, "zset");
        boolean withscores = false;
        long card = zcard(key);
        if (start < 0) {
            start = Math.max(card + start, 0);
        }
        if (stop < 0) {
            stop = card + stop;
        }
        List<ZsetPair> range = new ArrayList<>();
        if (!zsetCache.exists(key)) {
            return range;
        }
        if (options.length > 0 && options[0] != null && "withscores".equals(options[0].toLowerCase())) {
            withscores = true;
        }
        int count = 0;
        for (String member : zsetCache.get(key)) {
            if (start > count) {
                ++count;
                continue;
            }
            if (stop < count) {
                break;
            }
            ZsetPair pair = new ZsetPair(member);
            if (withscores) {
                pair.score = zsetCache.getScore(key, member);
            }
            range.add(pair);
            ++count;
        }
        return range;
    }

    @Override
    public synchronized List<ZsetPair> zrangebylex(final String key, String min, String max, String... options) {
        checkType(key, "zset");
        if (min.charAt(0) != '(' && min.charAt(0) != '[' && min.charAt(0) != '-' && min.charAt(0) != '+') {
            throw new NotValidStringRangeItemException();
        }
        if (max.charAt(0) != '(' && max.charAt(0) != '[' && max.charAt(0) != '-' && max.charAt(0) != '+') {
            throw new NotValidStringRangeItemException();
        }
        Set<ZsetPair> range = new TreeSet<>(ZsetPair.comparator());
        if (!zsetCache.exists(key)) {
            return range.stream().collect(Collectors.toList());
        }
        String minStr = min.substring(1);
        String maxStr = max.substring(1);
        boolean minInclusive = min.charAt(0) == '[';
        boolean maxInclusive = max.charAt(0) == '[';
        boolean maxAll = max.charAt(0) == '+';
        if (min.charAt(0) == '+') {
            return range.stream().collect(Collectors.toList());
        }
        if (max.charAt(0) == '-') {
            return range.stream().collect(Collectors.toList());
        }
        Set<String> members = zsetCache.get(key);
        for (String member : members) {
            if (member == null) {
                continue;
            }
            int minc = member.compareTo(minStr);
            int maxc = member.compareTo(maxStr);
            if (minc > 0 && (maxc < 0 || maxAll)) {
                range.add(new ZsetPair(member));
            } else if (minc == 0 && minInclusive) {
                range.add(new ZsetPair(member));
            } else if (maxc == 0 && maxInclusive) {
                range.add(new ZsetPair(member));
            }
        }
        return range.stream().collect(Collectors.toList());
    }

    @Override
    public synchronized List<ZsetPair> zrevrangebylex(final String key, final String max, final String min, final String... options) {
        List<ZsetPair> range = zrangebylex(key, min, max, options);
        Set<ZsetPair> revrange = new TreeSet<>(ZsetPair.descendingComparator());
        revrange.addAll(range);
        return revrange.stream().collect(Collectors.toList());
    }

    @Override
    public synchronized List<ZsetPair> zrangebyscore(final String key, String min, String max, String... options) {
        Double minf = null, maxf = null;
        boolean minInclusive = true, maxInclusive = true;
        checkType(key, "zset");
        if ("-inf".equals(min)) {
            minf = Double.MIN_VALUE;
        }
        if ("+inf".equals(min)) {
            minf = Double.MAX_VALUE;
        }
        if ("-inf".equals(max)) {
            maxf = Double.MIN_VALUE;
        }
        if ("+inf".equals(max)) {
            maxf = Double.MAX_VALUE;
        }
        if (min.charAt(0) == '(') {
            minInclusive = false;
            min = min.substring(1);
        }
        if (max.charAt(0) == '(') {
            maxInclusive = false;
            max = max.substring(1);
        }
        if (minf == null) {
            try {
                minf = Double.parseDouble(min);
            } catch (NumberFormatException e) {
                throw new NotFloatMinMaxException();
            }
        }
        if (maxf == null) {
            try {
                maxf = Double.parseDouble(max);
            } catch (NumberFormatException e) {
                throw new NotFloatMinMaxException();
            }
        }
        boolean withscores = false;
        long limitOffset = -1, limitCount = -1;
        for (int idx = 0; idx < options.length; ++idx) {
            String option = options[idx];
            if (option == null) {
                continue;
            }
            if ("withscores".equals(option.toLowerCase())) {
                withscores = true;
            }
            if ("limit".equals(option.toLowerCase())) {
                if (options.length <= idx + 2) {
                    throw new SyntaxErrorException();
                }
                try {
                    limitOffset = Long.parseLong(options[idx + 1]);
                    limitCount = Long.parseLong(options[idx + 2]);
                } catch (NumberFormatException e) {
                    throw new NotIntegerException();
                }
            }
        }
        Set<ZsetPair> range = new TreeSet<>(ZsetPair.comparator());
        if (!zsetCache.exists(key)) {
            return range.stream().collect(Collectors.toList());
        }
        Set<String> members = zsetCache.get(key);
        long offset = 0;
        for (String member : members) {
            if (member == null) {
                continue;
            }
            Double score = zsetCache.getScore(key, member);
            if (((minInclusive && minf <= score) || (!minInclusive && minf < score)) && ((maxInclusive && score <= maxf) || (!maxInclusive && score < maxf))) {
                if (limitOffset != -1 && offset >= limitOffset) {
                    if (limitCount != -1) {
                        if (range.size() < limitCount) {
                            if (withscores) {
                                range.add(new ZsetPair(member, score));
                            } else {
                                range.add(new ZsetPair(member));
                            }
                        } else {
                            break;
                        }
                    }
                } else if (limitOffset == -1) {
                    if (withscores) {
                        range.add(new ZsetPair(member, score));
                    } else {
                        range.add(new ZsetPair(member));
                    }
                }
            }
            ++offset;
        }
        return range.stream().collect(Collectors.toList());
    }

    @Override
    public synchronized Long zrank(final String key, final String member) throws WrongTypeException {
        checkType(key, "zset");
        if (!zsetCache.exists(key)) {
            return null;
        }
        if (zsetCache.getScore(key, member) == null) {
            return null;
        }
        Set<String> members = zsetCache.get(key);
        long rank = 0L;
        for (String m : members) {
            if (m.equals(member)) {
                break;
            }
            ++rank;
        }
        return rank;
    }

    @Override
    public synchronized Long zrem(final String key, final String member, final String... members) throws WrongTypeException {
        checkType(key, "zset");
        Long count = 0L;
        count += zsetCache.removeValue(key, member) ? 1L : 0L;
        for (String m : members) {
            count += zsetCache.removeValue(key, m) ? 1L : 0L;
        }
        if (zcard(key) == 0L) {
            del(key);
        }
        if (count > 0) {
            keyModified(key);
        }
        return count;
    }

    @Override
    public synchronized Long zremrangebylex(final String key, final String min, final String max) {
        List<ZsetPair> range = zrangebylex(key, min, max);
        if (!range.isEmpty()) {
            range.forEach(pair -> zsetCache.removeValue(key, pair.member));
            keyModified(key);
        }
        return (long) range.size();
    }

    @Override
    public synchronized Long zremrangebyrank(final String key, long min, long max) {
        checkType(key, "zset");
        Set<String> members = zsetCache.get(key);
        Set<String> toRem = new HashSet<>();
        long card = zcard(key);
        if (min < 0) {
            min = card + min;
        }
        if (max < 0) {
            max = card + max;
        }
        long count = 0L;
        for (String member : members) {
            if (min <= count && count <= max) {
                toRem.add(member);
            }
            if (count > max) {
                break;
            }
            ++count;
        }
        if (!toRem.isEmpty()) {
            toRem.forEach(rem -> zsetCache.removeValue(key, rem));
            keyModified(key);
        }
        return (long) toRem.size();
    }

    @Override
    public synchronized Long zremrangebyscore(final String key, final String min, final String max) {
        List<ZsetPair> range = zrangebyscore(key, min, max);
        for (ZsetPair pair : range) {
            zrem(key, pair.member);
        }
        return (long) range.size();
    }

    @Override
    public synchronized List<ZsetPair> zrevrange(final String key, final long start, final long stop, final String... options) throws WrongTypeException {
        List<ZsetPair> range = zrange(key, start, stop, options);
        Set<ZsetPair> revRange = new TreeSet<>(ZsetPair.descendingComparator());
        revRange.addAll(range);
        return revRange.stream().collect(Collectors.toList());
    }

    @Override
    public synchronized List<ZsetPair> zrevrangebyscore(final String key, final String max, final String min, final String... options) {
        List<ZsetPair> range = zrangebyscore(key, min, max, options);
        Set<ZsetPair> revRange = new TreeSet<>(ZsetPair.descendingComparator());
        revRange.addAll(range);
        return revRange.stream().collect(Collectors.toList());
    }

    @Override
    public synchronized Long zrevrank(final String key, final String member) {
        checkType(key, "zset");
        Long rank = zrank(key, member);
        if (rank == null) {
            return null;
        }
        return zcard(key) - rank - 1;
    }

    @Override
    public synchronized Double zscore(final String key, final String member) {
        checkType(key, "zset");
        return zsetCache.getScore(key, member);
    }

    @Override
    public synchronized Long zunionstore(final String destination, final int numkeys, final String... options) {
        if (exists(destination)) {
            del(destination);
        }
        ZsetAggregation zsetAggregation = ZsetAggregation.parse(numkeys, options);
        zsetAggregation.keys.forEach(key -> checkType(key, "zset"));
        String key = zsetAggregation.keys.get(0);
        List<ZsetPair> range = zrange(key, 0, -1, "withscores");
        Map<String, Double> rangeMap = new HashMap<>();
        for (ZsetPair pair : range) {
            if (zsetAggregation.weights.containsKey(key)) {
                pair.score *= zsetAggregation.weights.get(key);
            }
            rangeMap.put(pair.member, pair.score);
        }
        for (String k : zsetAggregation.keys.subList(1, numkeys)) {
            for (String m : zsetCache.get(k)) {
                ZsetPair pair = new ZsetPair(m, zsetCache.getScore(k, m));
                Double score = rangeMap.get(m);
                if (zsetAggregation.weights.containsKey(k)) {
                    pair.score *= zsetAggregation.weights.get(k);
                }
                pair.score = zsetAggregation.aggregation.aggregate(pair.score, score);
                rangeMap.put(pair.member, pair.score);
            }
        }
        Long count = 0L;
        for (String member : rangeMap.keySet()) {
            zadd(destination, new ZsetPair(member, rangeMap.get(member)));
            ++count;
        }
        return count;
    }

    @Override
    public synchronized ScanResult<List<ZsetPair>> zscan(String key, long cursor, String... options) {
        checkType(key, "zset");
        MatchAndCount matchAndCount = MatchAndCount.parse(options);
        Set<ZsetPair> scanned = new TreeSet<>(ZsetPair.comparator());
        List<ZsetPair> members = zrange(key, 0, -1, "withscores");
        Long idx = 0L;
        for (ZsetPair pair : members) {
            idx += 1;
            if (idx > cursor) {
                if (matchAndCount.matches(pair.member)) {
                    scanned.add(pair);
                }
                if ((long) scanned.size() >= matchAndCount.count) {
                    break;
                }
            }
        }
        if (idx >= zcard(key)) {
            idx = 0L;
        }
        return new ScanResult<>(idx, scanned.stream().collect(Collectors.toList()));
    }

    //endregion

    private static byte[] getBytes(String text) {
        int len = text.length();
        byte[] bytes = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            int code = text.codePointAt(i);
            bytes[2 * i + 1] = (byte) code;
            bytes[2 * i] = (byte) (code >> 8);
        }
        return bytes;
    }

    private static String toString(byte[] bytes) {
        assert bytes.length % 2 == 0;
        int len = bytes.length / 2;
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) ((bytes[2 * i] & 0xFF) << 8 | (bytes[2 * i + 1] & 0xFF));
        }
        return new String(chars);
    }

    private static class BitCommand {
        static class Type {
            private boolean unsigned;
            private long size;

            static Type parse(String text) {
                Type type = new Type();
                char signChar = text.charAt(0);
                switch (signChar) {
                    case 'i':
                        type.unsigned = false;
                        break;
                    case 'u':
                        type.unsigned = true;
                        break;
                    default:
                        throw new ArgException("bitfield");
                }
                type.size = Long.valueOf(text.substring(1));
                if (type.unsigned && type.size > 64) {
                    throw new ArgException("bitfield");
                }
                if (!type.unsigned && type.size > 63) {
                    throw new ArgException("bitfield");
                }
                return type;
            }
        }

        static class Offset {
            private boolean typed;
            private long offset;

            static Offset parse(String text) {
                Offset offset = new Offset();
                if (text.charAt(0) == '#') {
                    offset.typed = true;
                    offset.offset = Long.valueOf(text.substring(1));
                } else {
                    offset.typed = false;
                    offset.offset = Long.valueOf(text);
                }
                return offset;
            }
        }

        private Type type;
        private Offset offset;
        private String overflow;
        private String command;
        private Long value;

        long getActualOffset() {
            if (offset.typed) {
                return offset.offset * type.size;
            } else {
                return offset.offset;
            }
        }

        long exec(byte[] bytes) {
            int BYTE_LEN = 8;
            int LONG_LEN = 64;
            int pos = (int) (getActualOffset() / BYTE_LEN);
            int len = (int) ((getActualOffset() + type.size - 1) / BYTE_LEN + 1) - pos;
            byte[] ebytes = new byte[len];
            System.arraycopy(bytes, pos, ebytes, 0, len);
            int leftGap = (int) (getActualOffset() % BYTE_LEN);
            int rightGap = (BYTE_LEN - (int) (getActualOffset() + type.size) % BYTE_LEN) % BYTE_LEN;
            long oldValue = 0;
            for (int i = len - 1; i >= 0; i--) {
                int shift = rightGap - (len - 1 - i) * BYTE_LEN;
                if (shift > 0) {
                    oldValue |= (((long) (ebytes[i] & 0xFF)) >> shift);
                } else if (shift < 0) {
                    oldValue |= (((long) (ebytes[i] & 0xFF)) << -shift);
                } else {
                    oldValue |= ((long) (ebytes[i] & 0xFF));
                }
            }
            oldValue = oldValue << (LONG_LEN - type.size);
            if (type.unsigned) {
                oldValue = oldValue >>> (LONG_LEN - type.size);
            } else {
                oldValue = oldValue >> (LONG_LEN - type.size);
            }
            if (command.equals("get")) {
                return oldValue;
            }
            long value = oldValue;
            if (command.equals("set")) {
                value = this.value;
            } else if (command.equals("incrby")) {
                value += this.value;
            }
            //clear effective bits
            if (leftGap > 0) {
                //clear left-side bits = right shift + left shift
                ebytes[0] = (byte) ((ebytes[0] & 0xFF) >> (BYTE_LEN - leftGap) << (BYTE_LEN - leftGap));
            } else {
                ebytes[0] = 0;
            }
            if (rightGap > 0) {
                //clear right-side bits = left shift + unsigned right shift
                ebytes[len - 1] = (byte) (((ebytes[len - 1] & 0xFF) << (BYTE_LEN - rightGap)) & 0xFF >>> (BYTE_LEN - rightGap));
            } else {
                ebytes[len - 1] = 0;
            }
            for (int i = 1; i < len - 1; i++) {
                ebytes[i] = 0;
            }
            value = value << rightGap;
            for (int i = len - 1; i >= 0; i--) {
                if (i == 0 && len > 8) {
                    int firstTwoByteInteger = (int) (oldValue >>> (LONG_LEN - 16));
                    firstTwoByteInteger <<= rightGap;
                    firstTwoByteInteger = (firstTwoByteInteger << leftGap & 0xFFFF >> leftGap);
                    ebytes[0] |= (byte) (firstTwoByteInteger >> 8);
                } else {
                    ebytes[i] |= (byte) (value >> (len - 1 - i) * 8);
                }
            }
            System.arraycopy(ebytes, 0, bytes, pos, len);
            return oldValue;
        }

        static List<BitCommand> parse(String... options) {
            List<BitCommand> commands = new ArrayList<>();
            String currentOverflow = "WRAP";//default overflow
            int len = options.length;
            BitCommand command;
            for (int i = 0; i < len; ++i) {
                String cmd = options[i].toLowerCase();
                switch (cmd) {
                    case "get":
                        command = new BitCommand();
                        command.command = cmd;
                        command.type = Type.parse(options[++i]);
                        command.offset = Offset.parse(options[++i]);
                        command.overflow = currentOverflow;
                        commands.add(command);
                        break;
                    case "set":
                        command = new BitCommand();
                        command.command = cmd;
                        command.type = Type.parse(options[++i]);
                        command.offset = Offset.parse(options[++i]);
                        command.value = Long.valueOf(options[++i]);
                        command.overflow = currentOverflow;
                        commands.add(command);
                        break;
                    case "incrby":
                        command = new BitCommand();
                        command.command = cmd;
                        command.type = Type.parse(options[++i]);
                        command.offset = Offset.parse(options[++i]);
                        command.value = Long.valueOf(options[++i]);
                        command.overflow = currentOverflow;
                        commands.add(command);
                        break;
                    case "overflow":
                        currentOverflow = options[++i].toUpperCase();
                        break;
                    default:
                        throw new ArgException("bitfield");
                }
            }
            return commands;
        }
    }

    private static class MatchAndCount {
        private Pattern match;
        private Long count = 10L; //默认值:10

        public boolean matches(String text) {
            return match == null || match.matcher(text).matches();
        }

        static MatchAndCount parse(String... options) {
            MatchAndCount result = new MatchAndCount();
            for (int idx = 0; idx < options.length; ++idx) {
                if (options[idx].equals("count")) {
                    result.count = Long.valueOf(options[idx + 1]);
                } else if (options[idx].equals("match")) {
                    result.match = Pattern.compile(GlobToRegEx.convertGlobToRegEx(options[idx + 1]));
                }
            }
            return result;
        }
    }

    private static class ZsetAggregation {
        enum Aggregation {
            SUM, MIN, MAX;

            /**
             * score1 must not be null
             */
            Double aggregate(Double score1, Double score2) {
                switch (this) {
                    case MIN:
                        return score2 == null ? score1 : Math.min(score1, score2);
                    case MAX:
                        return score2 == null ? score1 : Math.max(score1, score2);
                    case SUM:
                        return score2 == null ? score1 : (score1 + score2);
                    default:
                        throw new IllegalArgumentException();
                }
            }

            static Aggregation from(String aggr) {
                return Enum.valueOf(Aggregation.class, aggr.toUpperCase());
            }
        }

        private List<String> keys = new ArrayList<>();
        private Map<String, Double> weights = new HashMap<>();
        private Aggregation aggregation = Aggregation.SUM;//默认值

        static ZsetAggregation parse(int numkeys, String... options) {
            ZsetAggregation zsetAggregation = new ZsetAggregation();
            int i;
            for (i = 0; i < numkeys; ++i) {
                zsetAggregation.keys.add(options[i]);
            }
            i = numkeys;
            while (i < options.length) {
                if (options[i] == null) {
                    continue;
                }
                if ("weights".equals(options[i].toLowerCase())) {
                    if (i + 1 >= options.length) {
                        throw new SyntaxErrorException();
                    }
                    int ki = 0;
                    ++i;
                    while (i < options.length && !("aggregate".equals(options[i]))) {
                        zsetAggregation.weights.put(zsetAggregation.keys.get(ki), Double.valueOf(options[i]));
                        ++ki;
                        ++i;
                    }
                } else if ("aggregate".equals(options[i].toLowerCase())) {
                    if (i + 1 >= options.length) {
                        throw new SyntaxErrorException();
                    }
                    zsetAggregation.aggregation = Aggregation.from(options[i + 1]);
                    i += 2;
                } else {
                    throw new SyntaxErrorException();
                }
            }
            return zsetAggregation;
        }
    }
}
