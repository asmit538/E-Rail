package com.erail.exceptions;

/**
 * CHECKED exception.
 * Thrown when a seat is already booked, does not exist, or a train has too few
 * free seats for the requested number of passengers.
 */
public class SeatNotAvailableException extends Exception {

    public SeatNotAvailableException(String message) {
        super(message);
    }
}
