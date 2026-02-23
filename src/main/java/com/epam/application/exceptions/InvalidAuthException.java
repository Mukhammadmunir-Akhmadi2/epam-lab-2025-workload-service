package com.epam.application.exceptions;

public class InvalidAuthException extends RuntimeException {
    public InvalidAuthException(String message) { super(message); }
}
