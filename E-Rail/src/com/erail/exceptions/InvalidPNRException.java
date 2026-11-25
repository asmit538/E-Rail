package com.erail.exceptions;

/**
 * CHECKED exception.
 * Thrown when a PNR has the wrong format (for example "ABC") or the format is
 * correct but no ticket with that PNR exists.
 */
public class InvalidPNRException extends Exception {

    public InvalidPNRException(String message) {
        super(message);
    }
}
