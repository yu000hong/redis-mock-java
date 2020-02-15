package org.rarefiedredis.redis.adapter.jedis;

import org.rarefiedredis.redis.IRedisClient;
import org.rarefiedredis.redis.exception.ArgException;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class JedisIRedisClient extends AbstractJedisIRedisClient {

    private final JedisPool pool;
    private final Jedis jedis;

    public JedisIRedisClient(JedisPool pool) {
        this.pool = pool;
        this.jedis = null;
    }

    public JedisIRedisClient(Jedis jedis) {
        this.pool = null;
        this.jedis = jedis;
    }

    @Override public IRedisClient createClient() {
        if (pool != null) {
            Jedis client = null;
            try {
                client = pool.getResource();
                return new JedisIRedisClient(client);
            }
            catch (Exception e) {
                if (client != null) {
                    client.close();
                }
            }
        }
        return this;
    }

    @Override public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override public Object command(final String name, final Object ... args) {
        Jedis jedis = null;
        Object ret = null;
        try {
            if (this.jedis != null) {
                jedis = this.jedis;
            }
            else {
                jedis = pool.getResource();
            }
            if (jedis == null) {
                return null;
            }
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
            ret = jedis
                .getClass()
                .getDeclaredMethod(name, parameterTypes)
                .invoke(jedis, args);
        }
        catch (NoSuchMethodException e) {
            ret = null;
        }
        catch (IllegalAccessException e) {
            ret = null;
        }
        catch (InvocationTargetException e) {
            ret = null;
            String msg = e.getCause().getMessage();
            if (msg.contains("WRONGTYPE")) {
                ret = new WrongTypeException();
            }
            else if (msg.contains("no such key")) {
                ret = new NoKeyException();
            }
            else if (msg.contains("index out of range")) {
                ret = new IndexOutOfRangeException();
            }
            else if (msg.contains("hash value is not an integer")) {
                ret = new NotIntegerHashException();
            }
            else if (msg.contains("value is not an integer")) {
                ret = new NotIntegerException();
            }
            else if (msg.contains("hash value is not a valid float")) {
                ret = new NotFloatHashException();
            }
            else if (msg.contains("value is not a valid float")) {
                ret = new NotFloatException();
            }
            else if (msg.contains("syntax error")) {
                ret = new SyntaxErrorException();
            }
            else if (msg.contains("wrong number of arguments")) {
                ret = new ArgException(e.getCause());
            }
            else if (msg.contains("not valid string range item")) {
                ret = new NotValidStringRangeItemException();
            }
            else if (msg.contains("min or max is not a float")) {
                ret = new NotFloatMinMaxException();
            }
        }
        finally {
            if (this.jedis == null && jedis != null) {
                jedis.close();
            }
        }
        return ret;
    }

    @Override public IRedisClient multi() {
        if (jedis != null) {
            return new JedisIRedisClientMulti(jedis);
        }
        return new JedisIRedisClientMulti(pool);
    }

    @Override public String watch(String key) {
        String[] keys = new String[1];
        keys[0] = key;
        // Are we using a pool? If so, cowardly bail.
        if (pool != null) {
            return null;
        }
        return (String)command("watch", new Object[] { keys });
    }

}
