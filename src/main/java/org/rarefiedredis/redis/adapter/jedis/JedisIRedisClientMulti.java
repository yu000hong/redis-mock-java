package org.rarefiedredis.redis.adapter.jedis;

import org.rarefiedredis.redis.IRedisClient;
import org.rarefiedredis.redis.exception.ExecWithoutMultiException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JedisIRedisClientMulti extends AbstractJedisIRedisClient {

    private final JedisPool pool;
    private Jedis jedis;
    private Transaction transaction;

    public JedisIRedisClientMulti(JedisPool pool) {
        this.pool = pool;
        this.jedis = null;
        initialize();
    }

    public JedisIRedisClientMulti(Jedis jedis) {
        this.pool = null;
        this.jedis = jedis;
        initialize();
    }

    private void initialize() {
        Jedis jedis = null;
        try {
            if (pool != null) {
                this.jedis = pool.getResource();
            }
            jedis = this.jedis;
            transaction = jedis.multi();
        }
        catch (Exception e) {
            transaction = null;
            if (pool != null && jedis != null) {
                jedis.close();
            }
        }
    }

    @Override public IRedisClient createClient() {
        return this;
    }

    @Override public Object command(String name, Object ... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int idx = 0; idx < args.length; ++idx) {
                if (args[idx] != null) {
                    parameterTypes[idx] = args[idx].getClass();
                    // Convert Object classes into primitive data type classes where appropriate.
                    // TODO: This sucks, but I don't have a better way right now
                    if (parameterTypes[idx].equals(Integer.class)) {
                        parameterTypes[idx] = int.class;
                    }
                    if (parameterTypes[idx].equals(Long.class)) {
                        parameterTypes[idx] = long.class;
                    }
                    if (parameterTypes[idx].equals(Double.class)) {
                        parameterTypes[idx] = double.class;
                    }
                    // Convert implementations into their interfaces where appropriate.
                    if (parameterTypes[idx].equals(HashMap.class)) {
                        parameterTypes[idx] = Map.class;
                    }
                }
            }
            synchronized (this) {
                if (transaction != null) {
                    transaction
                        .getClass()
                        .getDeclaredMethod(name, parameterTypes)
                        .invoke(transaction, args);
                }
            }
        }
        catch (NoSuchMethodException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (InvocationTargetException e) {
        }
        return null;
    }

    @Override public synchronized String discard() {
        String reply = transaction.discard();
        transaction = null;
        return reply;
    }

    @Override public List<Object> exec() throws ExecWithoutMultiException {
        List<Object> ret = null;
        synchronized (this) {
            if (transaction == null) {
                throw new ExecWithoutMultiException();
            }
            try {
                ret = transaction.exec();
            }
            catch (JedisDataException e) {
            }
            finally {
                if (pool != null && jedis != null) {
                    jedis.close();
                }
            }
        }
        return ret;
    }

    @Override public IRedisClient multi() {
        return this;
    }

}
