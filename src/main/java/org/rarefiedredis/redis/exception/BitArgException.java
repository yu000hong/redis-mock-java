package org.rarefiedredis.redis.exception;

public final class BitArgException extends RuntimeException {
    
    public BitArgException() {
        super("ERR The bit argument must be 1 or 0");
    }
}