package com.erail.exceptions;

/**
 * CHECKED exception (extends Exception).
 * Thrown when a train search finds nothing, or a train number does not exist.
 * Because it is checked, every method that may throw it must either catch it
 * or declare "throws TrainNotFoundException".
 */
public class TrainNotFoundException extends Exception {

    public TrainNotFoundException(String message) {
        super(message);
    }
}
