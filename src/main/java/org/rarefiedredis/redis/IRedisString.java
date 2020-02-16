package org.rarefiedredis.redis;

import java.util.List;

public interface IRedisString {

    Long append(String key, String value);

    Long bitcount(String key, long... options);

    Long bitop(String operation, String destKey, String... keys);

    Long bitpos(String key, long bit, long... options);

    Long decr(String key);

    Long decrby(String key, long decrement);

    String get(String key);

    Boolean getbit(String key, long offset);

    String getrange(String key, long start, long end);

    String getset(String key, String value);

    Long incr(String key);

    Long incrby(String key, long increment);

    String incrbyfloat(String key, double increment);

    String[] mget(String... keys);

    String mset(String... keyAndValues);

    Boolean msetnx(String... keyAndValues);

    String psetex(String key, long milliseconds, String value);

    String set(String key, String value, String... options);

    Long setbit(String key, long offset, boolean value);

    String setex(String key, int seconds, String value);

    Long setnx(String key, String value);

    Long setrange(String key, long offset, String value);

    Long strlen(String key);

    List<Long> bitfield(String key, String... options);

}
