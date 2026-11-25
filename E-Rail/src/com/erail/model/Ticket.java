package com.erail.model;

import com.erail.interfaces.Displayable;

/**
 * One ticket = one passenger on one train in one seat.
 * It HAS-A Passenger, a Train, a Coach and a Seat (object modelling).
 * Status moves PENDING -> CONFIRMED (after payment) or -> CANCELLED.
 */
public class Ticket implements Displayable {

    // Status constants (also used by Booking)
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String pnr;
    private int bookingId;
    private Passenger passenger;
    private Train train;
    private Coach coach;
    private Seat seat;
    private double fare;
    private String status;

    // Constructor 1: the status is given
    public Ticket(String pnr, Passenger passenger, Train train, Coach coach,
                  Seat seat, double fare, String status) {
        this.pnr = pnr;
        this.passenger = passenger;
        this.train = train;
        this.coach = coach;
        this.seat = seat;
        this.fare = fare;
        this.status = status;
        this.bookingId = 0;                    // set later by the Booking that owns this ticket
    }

    // Constructor 2 (overloading): a new ticket always starts as PENDING
    public Ticket(String pnr, Passenger passenger, Train train, Coach coach,
                  Seat seat, double fare) {
        this(pnr, passenger, train, coach, seat, fare, STATUS_PENDING);
    }

    public boolean isCancelled() {
        return status.equals(STATUS_CANCELLED);
    }

    // Builds the full multi-line ticket text using StringBuilder
    public String getTicketDetails() {
        Route route = train.getRoute();
        StringBuilder sb = new StringBuilder();
        sb.append("\n=========================== E-RAIL TICKET ===========================\n");
        sb.append(String.format(" PNR           : %s%n", pnr));
        sb.append(String.format(" Booking ID    : %d%n", bookingId));
        sb.append(String.format(" Status        : %s%n", status));
        sb.append("---------------------------------------------------------------------\n");
        sb.append(String.format(" Passenger     : %s%n", passenger.getDetails()));
        sb.append(String.format(" Train         : %d - %s%n", train.getTrainNumber(), train.getTrainName()));
        sb.append(String.format(" Journey       : %s (%s) -> %s (%s)%n",
                route.getSource().getName(), route.getSource().getCode(),
                route.getDestination().getName(), route.getDestination().getCode()));
        sb.append(String.format(" Departure     : %s%n", train.getDepartureTime()));
        sb.append(String.format(" Arrival       : %s%n", train.getArrivalTime()));
        sb.append(String.format(" Distance      : %d km   Duration: %s%n",
                route.getDistanceKm(), route.getDurationText()));
        sb.append(String.format(" Coach / Seat  : %s (%s) / seat %d (%s)%n",
                coach.getCoachNumber(), coach.getCoachType(),
                seat.getSeatNumber(), seat.getSeatType()));
        sb.append(String.format(" Fare          : Rs. %.2f%n", fare));
        sb.append("=====================================================================");
        return sb.toString();
    }

    public static String getTableHeader() {
        return String.format("%-10s %-25s %-6s %-6s %-5s %10s  %-9s",
                "PNR", "Passenger", "Train", "Coach", "Seat", "Fare", "Status");
    }

    @Override
    public String getSummary() {
        return String.format("%-10s %-25s %-6d %-6s %-5d %10.2f  %-9s",
                pnr, passenger.getName(), train.getTrainNumber(),
                coach.getCoachNumber(), seat.getSeatNumber(), fare, status);
    }

    public String getPnr() {
        return pnr;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public Train getTrain() {
        return train;
    }

    public Coach getCoach() {
        return coach;
    }

    public Seat getSeat() {
        return seat;
    }

    public double getFare() {
        return fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
