package com.erail.model;

import com.erail.exceptions.SeatNotAvailableException;
import com.erail.interfaces.Reservable;

/**
 * One coach of a train. Its seats are stored in a TWO-DIMENSIONAL array,
 * exactly like the seat layout in a real coach: seats[row][column].
 * Available seats are counted by looking at every Seat, so the number can
 * never get out of date.
 */
public class Coach implements Reservable {

    // Coach types (constants shared by the whole program)
    public static final String TYPE_SLEEPER = "Sleeper";
    public static final String TYPE_AC3 = "AC 3-Tier";
    public static final String TYPE_AC2 = "AC 2-Tier";

    private String coachNumber;
    private String coachType;
    private Seat[][] seats;

    // Constructor 1: the layout (rows x columns) is given
    public Coach(String coachNumber, String coachType, int rows, int columns) {
        this.coachNumber = coachNumber;
        this.coachType = coachType;
        this.seats = new Seat[rows][columns];

        int seatNumber = 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                // Seats at the left/right edge are window seats, the rest are aisle seats
                String seatType;
                if (col == 0 || col == columns - 1) {
                    seatType = "Window";
                } else {
                    seatType = "Aisle";
                }
                seats[row][col] = new Seat(seatNumber, seatType);
                seatNumber++;
            }
        }
    }

    // Constructor 2 (overloading): a standard 4 x 4 coach
    public Coach(String coachNumber, String coachType) {
        this(coachNumber, coachType, 4, 4);
    }

    // Reservable method: rows * columns
    @Override
    public int getTotalSeats() {
        return seats.length * seats[0].length;
    }

    // Reservable method: traverse the 2D array and count free seats
    @Override
    public int getAvailableSeats() {
        int count = 0;
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                if (seats[row][col].isAvailable()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean hasAvailableSeats() {
        return getAvailableSeats() > 0;
    }

    // Searches the 2D array for a seat number. Returns null if there is no such seat.
    public Seat findSeat(int seatNumber) {
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                if (seats[row][col].getSeatNumber() == seatNumber) {
                    return seats[row][col];
                }
            }
        }
        return null;
    }

    // Books a seat by its number and returns the Seat object
    public Seat bookSeat(int seatNumber) throws SeatNotAvailableException {
        Seat seat = findSeat(seatNumber);
        if (seat == null) {
            throw new SeatNotAvailableException(
                    "Seat " + seatNumber + " does not exist in coach " + coachNumber + ".");
        }
        seat.book();                           // may throw if the seat is already taken
        return seat;
    }

    // Prints the seat layout. [05] = free seat, [XX] = booked seat.
    public void displaySeatMap() {
        System.out.println("\nSeat map of coach " + coachNumber + " (" + coachType + ")");

        // First line shows W (Window) or A (Aisle) above each column
        for (int col = 0; col < seats[0].length; col++) {
            String typeLetter = seats[0][col].getSeatType().substring(0, 1);
            System.out.print(" " + typeLetter + "   ");
        }
        System.out.println();

        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                if (seats[row][col].isAvailable()) {
                    System.out.printf("[%02d] ", seats[row][col].getSeatNumber());
                } else {
                    System.out.print("[XX] ");
                }
            }
            System.out.println();
        }
        System.out.println("W = Window, A = Aisle, [XX] = already booked");
    }

    public String getCoachNumber() {
        return coachNumber;
    }

    public String getCoachType() {
        return coachType;
    }
}
