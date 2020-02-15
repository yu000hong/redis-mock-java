package org.rarefiedredis.redis.exception;

public final class NotFloatMinMaxException extends RuntimeException {
    
    public NotFloatMinMaxException() {
        super("ERR min or max is not a float");
    }
}
