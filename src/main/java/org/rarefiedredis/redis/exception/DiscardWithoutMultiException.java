package org.rarefiedredis.redis.exception;

public final class DiscardWithoutMultiException extends RuntimeException {
    
    public DiscardWithoutMultiException() {
        super("ERR DISCARD without MULTI");
    }
}
