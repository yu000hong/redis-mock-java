package org.rarefiedredis.redis.exception;

public final class NotFloatException extends RuntimeException {
    
    public NotFloatException() {
        super("ERR value is not a valid float");
    }
}
