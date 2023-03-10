package com.marizoo.user.exception;

public class AccessTokenException extends RuntimeException {
    public AccessTokenException() {
        super();
    }

    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessTokenException(Throwable cause) {
        super(cause);
    }
}
