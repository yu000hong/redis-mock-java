package org.rarefiedredis.redis.exception;

public final class ExecWithoutMultiException extends RuntimeException {
    
    public ExecWithoutMultiException() {
        super("ERR EXEC without MULTI");
    }
}
