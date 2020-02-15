package org.rarefiedredis.redis.exception;

public final class NotFloatHashException extends RuntimeException {
    
    public NotFloatHashException() {
        super("ERR hash value is not a valid float");
    }
}
