package org.rarefiedredis.redis;

import java.util.List;

public interface IRedisList {

    String lindex(String key, long index);

    Long linsert(String key, String beforeOrAfter, String pivot, String value);

    Long llen(String key);

    String lpop(String key);

    Long lpush(String key, String element, String... elements);

    Long lpushx(String key, String element);

    List<String> lrange(String key, long start, long stop);

    Long lrem(String key, long count, String element);

    String lset(String key, long index, String element);

    String ltrim(String key, long start, long stop);

    String rpop(String key);

    String rpoplpush(String source, String dest);

    Long rpush(String key, String element, String... elements);

    Long rpushx(String key, String element);

}
