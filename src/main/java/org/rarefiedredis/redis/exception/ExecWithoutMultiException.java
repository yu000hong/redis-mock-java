package org.rarefiedredis.redis.exception;

public final class ExecWithoutMultiException extends Exception {
    
    public ExecWithoutMultiException() {
        super("ERR EXEC without MULTI");
    }
}
