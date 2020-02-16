//package org.rarefiedredis.redis.adapter.jedis;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.rarefiedredis.redis.IRedisClient;
//import org.rarefiedredis.redis.RandomKey;
//import org.rarefiedredis.redis.exception.DiscardWithoutMultiException;
//import org.rarefiedredis.redis.exception.ExecWithoutMultiException;
//import org.rarefiedredis.redis.exception.NotImplementedException;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.JedisPoolConfig;
//
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//
//public class JedisIRedisClientTransactionIT {
//
//    private JedisPool pool;
//    private IRedisClient redis;
//    private RandomKey rander;
//
//    @Before public void initPool() {
//        pool = new JedisPool(new JedisPoolConfig(), "localhost");
//        redis = new JedisIRedisClient(pool);
//        rander = new RandomKey();
//    }
//
//    @Test public void discardShouldThrowAnErrorIfWeAreNotInAMulti() {
//        try {
//            redis.discard();
//        }
//        catch (DiscardWithoutMultiException dwme) {
//            assertEquals(true, true);
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void discardShouldDiscardTheMulti() throws NotImplementedException {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3";
//        IRedisClient multi = redis.multi();
//        try {
//            multi.lpush(k, v1);
//            multi.lpush(k, v2);
//            multi.lpush(k, v3);
//            multi.discard();
//            multi.sadd(k, v1);
//            multi.sadd(k, v2);
//            List<Object> replies = multi.exec();
//        }
//        catch (ExecWithoutMultiException e) {
//            assertEquals("none", redis.type(k));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Ignore("pending") @Test public void discardShouldUnwatchAllWatchedKeys() {
//    }
//
//    @Test public void watchShouldWatchAKeyAndFailAMultiIfTheKeyChanges() throws NotImplementedException {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3";
//        String v = "v";
//        IRedisClient client = redis.createClient();
//        IRedisClient other = redis.createClient();
//        assertEquals("OK", client.watch(k));
//        try {
//            IRedisClient multi = client.multi();
//            multi.lpush(k, v1);
//            multi.lpush(k, v2);
//            multi.lpush(k, v3);
//            other.lpush(k, v);
//            List<Object> replies = multi.exec();
//            assertEquals(null, replies);
//            assertEquals("list", redis.type(k));
//            assertEquals(1L, (long)redis.llen(k));
//            assertEquals(v, redis.lindex(k, 0L));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void watchShouldStopWatchingAllKeysForThatClientAfterExec() throws NotImplementedException {
//        String k1 = rander.randkey(), k2 = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3";
//        String v = "v";
//        IRedisClient client = redis.createClient();
//        IRedisClient other = redis.createClient();
//        assertEquals("OK", client.watch(k1));
//        assertEquals("OK", client.watch(k2));
//        try {
//            IRedisClient multi = client.multi();
//            multi.lpush(k1, v1);
//            multi.rpush(k2, v2);
//            multi.lpush(k1, v3);
//            other.lpush(k1, v); // Should fail the exec.
//            List<Object> replies = multi.exec();
//            assertEquals(null, replies);
//            multi = client.multi();
//            multi.lpush(k1, v1);
//            multi.rpush(k2, v2);
//            multi.lpush(k1, v3);
//            other.lpush(k2, v); // Should _not_ fail the exec.
//            replies = multi.exec();
//            assertEquals(3, replies.size());
//            assertEquals(2L, (long)redis.llen(k2));
//            assertEquals(3L, (long)redis.llen(k1));
//            assertEquals(true, redis.lrange(k1, 0, -1).contains(v1));
//            assertEquals(true, redis.lrange(k1, 0, -1).contains(v3));
//            assertEquals(true, redis.lrange(k1, 0, -1).contains(v));
//            assertEquals(true, redis.lrange(k2, 0, -1).contains(v2));
//            assertEquals(true, redis.lrange(k2, 0, -1).contains(v));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Ignore("pending") @Test public void watchShouldWatchAKeyAndFailAMultiThatHasACommandThatTakesInMultipleKeysIfOneOfTheKeysChanged() {
//    }
//
//    @Ignore("pending") @Test public void watchShouldBeWatchingOnEveryModificationCommand() {
//    }
//
//    @Test public void watchShouldFailAllClientExecsThatAreWatchingTheSameKeyIfTheKeyChanges() throws NotImplementedException {
//        String k = rander.randkey();
//        String v = "v";
//        IRedisClient c1 = redis.createClient(), c2 = redis.createClient(), c3 = redis.createClient();
//        try {
//            assertEquals("OK", c1.watch(k));
//            assertEquals("OK", c2.watch(k));
//            assertEquals("OK", c3.watch(k));
//            redis.lpush(k, v);
//            IRedisClient m1 = c1.multi();
//            IRedisClient m2 = c2.multi();
//            IRedisClient m3 = c3.multi();
//            m1.set(k, v);
//            m2.sadd(k, v);
//            m3.hset(k, v, v);
//            List<Object> replies = m1.exec();
//            assertEquals(null, replies);
//            replies = m2.exec();
//            assertEquals(null, replies);
//            replies = m3.exec();
//            assertEquals(null, replies);
//            assertEquals("list", redis.type(k));
//            assertEquals(1L, (long)redis.llen(k));
//            assertEquals(v, redis.lindex(k, 0L));
//            return;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void multiShouldExecuteAMultiCommand() {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4";
//        try {
//            IRedisClient multi = redis.multi();
//            multi.lpush(k, v1, v3, v4);
//            multi.rpush(k, v2);
//            List<Object> replies = multi.exec();
//            assertEquals(2, replies.size());
//            assertEquals(3L, (long)(Long)replies.get(0));
//            assertEquals(4L, (long)(Long)replies.get(1));
//            assertEquals(4L, (long)redis.llen(k));
//            assertEquals(v4, redis.lindex(k, 0L));
//            assertEquals(v3, redis.lindex(k, 1L));
//            assertEquals(v1, redis.lindex(k, 2L));
//            assertEquals(v2, redis.lindex(k, 3L));
//            return;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void multiShouldFailIfAKeyInAMultiCommandIsBeingWatchedAndChanges() throws NotImplementedException {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3";
//        String v = "v";
//        IRedisClient client = redis.createClient();
//        IRedisClient other = redis.createClient();
//        assertEquals("OK", client.watch(k));
//        try {
//            IRedisClient multi = client.multi();
//            multi.lpush(k, v1);
//            multi.rpush(k, v2);
//            multi.lpush(k, v3);
//            other.set(k, v);
//            List<Object> replies = multi.exec();
//            assertEquals(null, replies);
//            assertEquals(v, redis.get(k));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void multiShouldNotFailIfTheKeysAreNotBeingWatched() {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4";
//        String v = "v";
//        try {
//            IRedisClient multi = redis.multi();
//            multi.del(k);
//            multi.lpush(k, v1, v3, v4);
//            multi.rpush(k, v2);
//            redis.set(k, v);
//            List<Object> replies = multi.exec();
//            assertEquals(3, replies.size());
//            assertEquals(3L, (long)(Long)replies.get(1));
//            assertEquals(4L, (long)(Long)replies.get(2));
//            assertEquals("list", redis.type(k));
//            assertEquals(4L, (long)redis.llen(k));
//            assertEquals(v4, redis.lindex(k, 0L));
//            assertEquals(v3, redis.lindex(k, 1L));
//            assertEquals(v1, redis.lindex(k, 2L));
//            assertEquals(v2, redis.lindex(k, 3L));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void multiShouldDoLsetAndLrem() {
//        String k = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4";
//        String v = "v";
//        try {
//            redis.rpush(k, v1, v2, v3, v4);
//            IRedisClient multi = redis.multi();
//            multi.lset(k, 1L, v);
//            multi.lrem(k, 1L, v);
//            multi.exec();
//            assertEquals(3L, (long)redis.llen(k));
//            assertEquals(v3, redis.lindex(k, 1L));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(false, true);
//    }
//
//    @Test public void multiShouldSmove() {
//        String k = rander.randkey(), d = rander.randkey();
//        String v1 = "v1", v2 = "v2", v3 = "v3", v4 = "v4";
//        try {
//            redis.sadd(k, v1, v2, v3, v4);
//            IRedisClient multi = redis.multi();
//            multi.smove(k, d, v2);
//            multi.smove(k, d, v1);
//            multi.smove(k, d, "v");
//            List<Object> replies = multi.exec();
//            assertEquals(3, replies.size());
//            assertEquals(true, redis.sismember(d, v1));
//            assertEquals(true, redis.sismember(d, v2));
//            assertEquals(false, redis.sismember(d, "v"));
//            assertEquals(false, redis.sismember(k, v1));
//            assertEquals(false, redis.sismember(k, v2));
//            assertEquals(2L, (long)redis.scard(k));
//            return;
//        }
//        catch (Exception e) {
//        }
//        assertEquals(true, false);
//    }
//
//    @Ignore("pending") @Test public void multiShouldBeAbleToExecuteEveryCommand() {
//    }
//
//    @Ignore("pending") @Test public void unwatchShouldUnwatch() {
//    }
//
//}
