package org.rarefiedredis.redis;

import org.rarefiedredis.redis.exception.NotImplementedException;

public interface IRedisKeys {

    Long del(String... keys) throws NotImplementedException;

    String dump(String key) throws NotImplementedException;

    Boolean exists(String key) throws NotImplementedException;

    Boolean expire(String key, int seconds) throws NotImplementedException;

    Boolean expireat(String key, long timestamp) throws NotImplementedException;

    String[] keys(String pattern) throws NotImplementedException;

    String migrate(String host, int port, String key, String destination_db, int timeout, String... options) throws NotImplementedException;

    Long move(String key, int db) throws NotImplementedException;

    Object object(String subcommand, String... arguments) throws NotImplementedException;

    Boolean persist(String key) throws NotImplementedException;

    Boolean pexpire(String key, long milliseconds) throws NotImplementedException;

    Boolean pexpireat(String key, long timestamp) throws NotImplementedException;

    Long pttl(String key) throws NotImplementedException;

    String randomkey() throws NotImplementedException;

    String rename(String key, String newKey) throws NotImplementedException;

    Boolean renamenx(String key, String newKey) throws NotImplementedException;

    String restore(String key, int ttl, String serialized_value) throws NotImplementedException;

    String[] sort(String key, String... options) throws NotImplementedException;

    Long ttl(String key) throws NotImplementedException;

    String type(String key) throws NotImplementedException;

    String[] scan(int cursor) throws NotImplementedException;

}
