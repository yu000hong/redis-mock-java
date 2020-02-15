package org.rarefiedredis.redis.exception;

public final class IndexOutOfRangeException extends Exception {
    
    public IndexOutOfRangeException() {
        super("ERR index out of range");
    }
}
