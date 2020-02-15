package org.rarefiedredis.redis.exception;

public final class NotIntegerHashException extends RuntimeException {
    
    public NotIntegerHashException() {
        super("ERR hash value is not an integer");
    }
}
