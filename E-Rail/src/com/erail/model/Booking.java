package com.erail.model;

import java.time.LocalDate;

/**
 * One booking = one reservation request made by a user. It can contain up to
 * four passengers, so it stores arrays: Passenger[] and Ticket[]
 * (passengers[i] travels on tickets[i]). It also holds the Payment, once one is made.
 */
public class Booking {

    // static: one shared counter gives every booking a unique ID
    private static int nextBookingId = 1001;

    private int bookingId;
    private String bookingDate;
    private Passenger[] passengers;
    private Ticket[] tickets;
    private Payment payment;                   // null until the user tries to pay
    private String status;

    // Constructor 1: the booking date is given
    public Booking(Passenger[] passengers, Ticket[] tickets, String bookingDate) {
        this.bookingId = nextBookingId;
        nextBookingId++;
        this.bookingDate = bookingDate;
        this.passengers = passengers;
        this.tickets = tickets;
        this.payment = null;
        this.status = Ticket.STATUS_PENDING;

        // Tell every ticket which booking it belongs to
        for (int i = 0; i < tickets.length; i++) {
            tickets[i].setBookingId(bookingId);
        }
    }

    // Constructor 2 (overloading): use today's date
    public Booking(Passenger[] passengers, Ticket[] tickets) {
        this(passengers, tickets, LocalDate.now().toString());
    }

    // Number of tickets that are not cancelled
    public int getActiveTicketCount() {
        int count = 0;
        for (int i = 0; i < tickets.length; i++) {
            if (!tickets[i].isCancelled()) {
                count++;
            }
        }
        return count;
    }

    public boolean isPaid() {
        return payment != null && payment.isSuccessful();
    }

    // Text for reports: "UNPAID" if no payment was tried, otherwise SUCCESS / FAILED
    public String getPaymentStatus() {
        if (payment == null) {
            return "UNPAID";
        }
        return payment.getStatus();
    }

    // Recalculates the booking status from its tickets and payment.
    // Called after every payment or cancellation, so the rule lives in ONE place.
    public void refreshStatus() {
        if (getActiveTicketCount() == 0) {
            status = Ticket.STATUS_CANCELLED;
        } else if (isPaid()) {
            status = Ticket.STATUS_CONFIRMED;
        } else {
            status = Ticket.STATUS_PENDING;
        }
    }

    public int getTrainNumber() {
        return tickets[0].getTrain().getTrainNumber();
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public Passenger[] getPassengers() {
        return passengers;
    }

    public Ticket[] getTickets() {
        return tickets;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getStatus() {
        return status;
    }
}
