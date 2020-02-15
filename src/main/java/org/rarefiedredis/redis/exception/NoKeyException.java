package org.rarefiedredis.redis.exception;

public final class NoKeyException extends RuntimeException {
    
    public NoKeyException() {
        super("ERR no such key");
    }
}
