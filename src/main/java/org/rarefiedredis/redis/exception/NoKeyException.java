package org.rarefiedredis.redis.exception;

public final class NoKeyException extends Exception {
    
    public NoKeyException() {
        super("ERR no such key");
    }
}
