package org.rarefiedredis.redis;

import org.rarefiedredis.redis.exception.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRedisClient implements IRedisClient {

    public AbstractRedisClient() {
    }

    @Override
    public void close() {
    }

    //region IRedisKeys commands 

    @Override
    public Long del(String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public String dump(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean exists(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean expire(String key, int seconds) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean expireat(String key, long timestamp) {
        throw new NotImplementedException();
    }

    @Override
    public String[] keys(String pattern) {
        throw new NotImplementedException();
    }

    @Override
    public String migrate(String host, int port, String key, String destination_db, int timeout, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long move(String key, int db) {
        throw new NotImplementedException();
    }

    @Override
    public Object object(String subcommand, String... arguments) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean persist(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean pexpire(String key, long milliseconds) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean pexpireat(String key, long timestamp) {
        throw new NotImplementedException();
    }

    @Override
    public Long pttl(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String randomkey() {
        throw new NotImplementedException();
    }

    @Override
    public String rename(String key, String newKey) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean renamenx(String key, String newKey) {
        throw new NotImplementedException();
    }

    @Override
    public String restore(String key, int ttl, String serialized_valued) {
        throw new NotImplementedException();
    }

    @Override
    public String[] sort(String key, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long ttl(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String type(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String[] scan(int cursor) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisString commands 

    @Override
    public Long append(String key, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long bitcount(String key, long... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long bitop(String operation, String destkey, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Long bitpos(String key, long bit, long... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long decr(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long decrby(String key, long decrement) {
        throw new NotImplementedException();
    }

    @Override
    public String get(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean getbit(String key, long offset) {
        throw new NotImplementedException();
    }

    @Override
    public String getrange(String key, long start, long end) {
        throw new NotImplementedException();
    }

    @Override
    public String getset(String key, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long incr(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long incrby(String key, long increment) {
        throw new NotImplementedException();
    }

    @Override
    public String incrbyfloat(String key, double increment) {
        throw new NotImplementedException();
    }

    @Override
    public String[] mget(String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public String mset(String... keyvalues) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean msetnx(String... keyvalues) {
        throw new NotImplementedException();
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        throw new NotImplementedException();
    }

    @Override
    public String set(String key, String value, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long setbit(String key, long offset, boolean value) {
        throw new NotImplementedException();
    }

    @Override
    public String setex(String key, int seconds, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long setnx(String key, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long strlen(String key) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisList commands 

    @Override
    public String lindex(String key, long index) {
        throw new NotImplementedException();
    }

    @Override
    public Long linsert(String key, String before_after, String pivot, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long llen(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String lpop(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long lpush(String key, String element, String... elements) {
        throw new NotImplementedException();
    }

    @Override
    public Long lpushx(String key, String element) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        throw new NotImplementedException();
    }

    @Override
    public Long lrem(String key, long count, String element) {
        throw new NotImplementedException();
    }

    @Override
    public String lset(String key, long index, String element) {
        throw new NotImplementedException();
    }

    @Override
    public String ltrim(String key, long start, long end) {
        throw new NotImplementedException();
    }

    @Override
    public String rpop(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String rpoplpush(String source, String dest) {
        throw new NotImplementedException();
    }

    @Override
    public Long rpush(String key, String element, String... elements) {
        throw new NotImplementedException();
    }

    @Override
    public Long rpushx(String key, String element) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisSetCommands 

    @Override
    public Long sadd(String key, String member, String... members) {
        throw new NotImplementedException();
    }

    @Override
    public Long scard(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> sdiff(String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Long sdiffstore(String destination, String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> sinter(String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Long sinterstore(String destination, String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean sismember(String key, String member) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> smembers(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean smove(String source, String dest, String member) {
        throw new NotImplementedException();
    }

    @Override
    public String spop(String key) {
        throw new NotImplementedException();
    }

    @Override
    public String srandmember(String key) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> srandmember(String key, long count) {
        throw new NotImplementedException();
    }

    @Override
    public Long srem(String key, String member, String... members) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> sunion(String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public Long sunionstore(String destination, String key, String... keys) {
        throw new NotImplementedException();
    }

    @Override
    public ScanResult<Set<String>> sscan(String key, long cursor, String... options) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisHash commands 

    @Override
    public Long hdel(String key, String field, String... fields) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean hexists(String key, String field) {
        throw new NotImplementedException();
    }

    @Override
    public String hget(String key, String field) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> hgetall(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long hincrby(String key, String field, long increment) {
        throw new NotImplementedException();
    }

    @Override
    public String hincrbyfloat(String key, String field, double increment) {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> hkeys(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long hlen(String key) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> hmget(String key, String field, String... fields) {
        throw new NotImplementedException();
    }

    @Override
    public void hmset(String key, String field, String value, String... fieldsvalues) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean hset(String key, String field, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Boolean hsetnx(String key, String field, String value) {
        throw new NotImplementedException();
    }

    @Override
    public Long hstrlen(String key, String field) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> hvals(String key) {
        throw new NotImplementedException();
    }

    @Override
    public ScanResult<Map<String, String>> hscan(String key, long cursor, String... options) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisTransaction commands 

    @Override
    public String discard() {
        throw new NotImplementedException();
    }

    @Override
    public List<Object> exec() {
        throw new NotImplementedException();
    }

    @Override
    public IRedisClient multi() {
        throw new NotImplementedException();
    }

    @Override
    public String unwatch() {
        throw new NotImplementedException();
    }

    @Override
    public String watch(String key) {
        throw new NotImplementedException();
    }

    //endregion

    //region IRedisSortedSet commands 

    @Override
    public Long zadd(String key, ZsetPair scoremember, ZsetPair... scoresmembers) {
        throw new NotImplementedException();
    }

    @Override
    public Long zadd(String key, double score, String member, Object... scoresmembers) {
        throw new NotImplementedException();
    }

    @Override
    public Long zcard(String key) {
        throw new NotImplementedException();
    }

    @Override
    public Long zcount(String key, double min, double max) {
        throw new NotImplementedException();
    }

    @Override
    public String zincrby(String key, double increment, String member) {
        throw new NotImplementedException();
    }

    @Override
    public Long zinterstore(String destination, int numkeys, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrange(String key, long start, long stop, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrangebylex(String key, String min, String max, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrevrangebylex(String key, String max, String min, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrangebyscore(String key, String min, String max, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long zrank(String key, String member) {
        throw new NotImplementedException();
    }

    @Override
    public Long zrem(String key, String member, String... members) {
        throw new NotImplementedException();
    }

    @Override
    public Long zremrangebylex(String key, String min, String max) {
        throw new NotImplementedException();
    }

    @Override
    public Long zremrangebyrank(String key, long start, long stop) {
        throw new NotImplementedException();
    }

    @Override
    public Long zremrangebyscore(String key, String min, String max) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrevrange(String key, long start, long stop, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Set<ZsetPair> zrevrangebyscore(String key, String max, String min, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public Long zrevrank(String key, String member) {
        throw new NotImplementedException();
    }

    @Override
    public Double zscore(String key, String member) {
        throw new NotImplementedException();
    }

    @Override
    public Long zunionstore(String destination, int numkeys, String... options) {
        throw new NotImplementedException();
    }

    @Override
    public ScanResult<Set<ZsetPair>> zscan(String key, long cursor, String... options) {
        throw new NotImplementedException();
    }

    //endregion

}
