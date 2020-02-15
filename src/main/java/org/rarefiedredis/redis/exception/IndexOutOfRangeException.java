package org.rarefiedredis.redis.exception;

public final class IndexOutOfRangeException extends RuntimeException {
    
    public IndexOutOfRangeException() {
        super("ERR index out of range");
    }
}
