package com.erail.interfaces;

/**
 * Anything that owns seats which can be counted: a whole Train or a single Coach.
 * Both classes promise to answer these questions, but each answers in its own way
 * (a Train adds up its coaches, a Coach counts its seats).
 */
public interface Reservable {

    // Total number of seats (booked + free)
    int getTotalSeats();

    // Number of seats that are still free
    int getAvailableSeats();

    // true if at least one seat is free
    boolean hasAvailableSeats();
}
