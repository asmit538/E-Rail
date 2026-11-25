package com.erail.model;

import com.erail.exceptions.SeatNotAvailableException;

/**
 * One seat inside a Coach. It knows its number, its type (Window / Aisle)
 * and whether it is free.
 */
public class Seat {

    private int seatNumber;
    private String seatType;
    private boolean available;

    public Seat(int seatNumber, String seatType) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.available = true;                 // every new seat starts free
    }

    // Marks the seat as booked. Throws if somebody already has it.
    public void book() throws SeatNotAvailableException {
        if (!available) {
            throw new SeatNotAvailableException("Seat " + seatNumber + " is already booked.");
        }
        available = false;
    }

    // Frees the seat again (used when a ticket is cancelled)
    public void release() {
        available = true;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public boolean isAvailable() {
        return available;
    }
}
