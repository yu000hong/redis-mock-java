package org.rarefiedredis.redis.exception;

public final class ArgException extends RuntimeException {
    
    public ArgException(String command) {
        super("ERR wrong number of arguments for \'" + command + "\' command");
    }

    public ArgException(Throwable t) {
        super(t.getMessage());
    }

}
