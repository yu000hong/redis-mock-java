package org.rarefiedredis.redis.exception;

/**
 * Thrown when a redis command encounters a syntax error.
 */
public final class SyntaxErrorException extends RuntimeException {
    /**
     * Constructor. Makes the exception with 'ERR syntax error'
     * as the message.
     */
    public SyntaxErrorException() {
        super("ERR syntax error");
    }
}
