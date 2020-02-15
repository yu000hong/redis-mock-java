package org.rarefiedredis.redis.exception;

public final class NotIntegerException extends RuntimeException {
    
    public NotIntegerException() {
        super("ERR value is not an integer or out of range");
    }
}
