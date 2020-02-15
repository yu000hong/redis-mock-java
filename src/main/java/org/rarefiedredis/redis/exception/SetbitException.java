package org.rarefiedredis.redis.exception;

public final class SetbitException extends RuntimeException {
    
    public SetbitException() {
        super("ERR bit is not an integer or out of range");
    }
}
