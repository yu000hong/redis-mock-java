package org.rarefiedredis.redis.exception;

public final class DiscardWithoutMultiException extends Exception {
    
    public DiscardWithoutMultiException() {
        super("ERR DISCARD without MULTI");
    }
}
