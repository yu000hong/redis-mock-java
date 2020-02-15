package org.rarefiedredis.redis;

import java.util.List;

public abstract class AbstractRedisMock extends AbstractRedisClient {

    public abstract boolean modified(Integer hashCode, String command, List<Object> args);

}
