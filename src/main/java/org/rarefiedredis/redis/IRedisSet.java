package org.rarefiedredis.redis;

import java.util.Set;

public interface IRedisSet {

    Long sadd(String key, String member, String... members);

    Long scard(String key);

    Set<String> sdiff(String key, String... keys);

    Long sdiffstore(String destination, String key, String... keys);

    Set<String> sinter(String key, String... keys);

    Long sinterstore(String destination, String key, String... keys);

    Boolean sismember(String key, String member);

    Set<String> smembers(String key);

    Boolean smove(String source, String dest, String member);

    Set<String> spop(String key, long count);

    Set<String> srandmember(String key, long count);

    Long srem(String key, String member, String... members);

    ScanResult<Set<String>> sscan(String key, long cursor, String... options);

    Set<String> sunion(String key, String... keys);

    Long sunionstore(String destination, String key, String... keys);

}
