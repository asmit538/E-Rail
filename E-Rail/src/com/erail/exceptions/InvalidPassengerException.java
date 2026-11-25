package com.erail.exceptions;

/**
 * UNCHECKED exception (extends RuntimeException).
 * Thrown when a passenger's name, age or gender is invalid.
 * Because it is unchecked, methods do not have to declare it, but we still
 * catch it in Main so the program never crashes on bad input.
 */
public class InvalidPassengerException extends RuntimeException {

    public InvalidPassengerException(String message) {
        super(message);
    }
}
