package org.rarefiedredis.redis.exception;

/**
 * Thrown when a redis command is attempted on a key that
 * holds a different type of key.
 */
public final class WrongTypeException extends RuntimeException {
    /**
     * Constructor. Makes the exception with 'WRONGTYPE Operation
     * against a key holding the wrong kind of value' as the
     * message.
     */
    public WrongTypeException() {
        super("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
}
