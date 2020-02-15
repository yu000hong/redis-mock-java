package org.rarefiedredis.redis;

import org.rarefiedredis.redis.exception.DiscardWithoutMultiException;
import org.rarefiedredis.redis.exception.ExecWithoutMultiException;
import org.rarefiedredis.redis.exception.NotImplementedException;

import java.util.List;

public interface IRedisTransaction {

    String discard() throws DiscardWithoutMultiException, NotImplementedException;

    List<Object> exec() throws ExecWithoutMultiException, NotImplementedException;

    IRedisClient multi() throws NotImplementedException;

    String unwatch() throws NotImplementedException;

    String watch(String key) throws NotImplementedException;

}
