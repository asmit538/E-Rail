package com.erail.service;

import com.erail.model.Booking;
import com.erail.model.Passenger;
import com.erail.model.Payment;
import com.erail.model.Ticket;
import com.erail.model.User;

/**
 * Read-only output: booking lists, booking details, passenger lists and statistics.
 * It never changes any booking.
 */
public class ReportService {

    private BookingService bookingService;
    private TicketService ticketService;
    private FareCalculator fareCalculator;

    public ReportService(BookingService bookingService, TicketService ticketService,
                         FareCalculator fareCalculator) {
        this.bookingService = bookingService;
        this.ticketService = ticketService;
        this.fareCalculator = fareCalculator;
    }

    // ------------------------------------------------------------ booking lists

    public void displayAllBookings() {
        displayBookings("ALL BOOKINGS", null);
    }

    public void displayConfirmedBookings() {
        displayBookings("CONFIRMED BOOKINGS", Ticket.STATUS_CONFIRMED);
    }

    public void displayCancelledBookings() {
        displayBookings("CANCELLED BOOKINGS", Ticket.STATUS_CANCELLED);
    }

    public void displayPendingBookings() {
        displayBookings("PENDING (UNPAID) BOOKINGS", Ticket.STATUS_PENDING);
    }

    // Shared by all four methods above. statusFilter == null means "show every booking".
    private void displayBookings(String title, String statusFilter) {
        Booking[] bookings = bookingService.getBookings();
        int total = bookingService.getBookingCount();

        System.out.println("\n" + title);
        System.out.printf("%-8s %-12s %-6s %-5s %11s  %-9s  %-9s%n",
                "Booking", "Date", "Train", "Pax", "Amount", "Payment", "Status");
        System.out.println("-----------------------------------------------------------------");

        int shown = 0;
        for (int i = 0; i < total; i++) {
            Booking booking = bookings[i];
            if (statusFilter == null || booking.getStatus().equals(statusFilter)) {
                String passengers = booking.getActiveTicketCount() + "/" + booking.getTickets().length;
                double amount = fareCalculator.calculateTotalAmount(booking.getTickets());
                System.out.printf("%-8d %-12s %-6d %-5s %11.2f  %-9s  %-9s%n",
                        booking.getBookingId(), booking.getBookingDate(),
                        booking.getTrainNumber(), passengers, amount,
                        booking.getPaymentStatus(), booking.getStatus());
                shown++;
            }
        }

        if (shown == 0) {
            System.out.println("No bookings found.");
        } else {
            System.out.println("Total shown: " + shown);
            System.out.println("(Pax = active tickets / total tickets. Amount counts active tickets only.)");
        }
    }

    // ------------------------------------------------------------ booking details

    // Builds the text with StringBuilder, then prints it with the ticket table
    public void displayBookingDetails(Booking booking) {
        double amount = fareCalculator.calculateTotalAmount(booking.getTickets());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n------------------ BOOKING %d ------------------%n", booking.getBookingId()));
        sb.append(String.format(" Booking date   : %s%n", booking.getBookingDate()));
        sb.append(String.format(" Train          : %d - %s%n", booking.getTrainNumber(),
                booking.getTickets()[0].getTrain().getTrainName()));
        sb.append(String.format(" Booking status : %s%n", booking.getStatus()));
        sb.append(String.format(" Payment status : %s%n", booking.getPaymentStatus()));
        sb.append(String.format(" Amount (active): Rs. %.2f", amount));
        System.out.println(sb.toString());

        System.out.println();
        System.out.println(Ticket.getTableHeader());
        System.out.println("-----------------------------------------------------------------------");
        Ticket[] tickets = booking.getTickets();
        for (int i = 0; i < tickets.length; i++) {
            System.out.println(tickets[i].getSummary());
        }
    }

    // ------------------------------------------------------------ passengers

    // Polymorphism: each Passenger is handled as a User, and getDetails() runs
    // the version written in Passenger.
    public void displayPassengersOfBooking(Booking booking) {
        System.out.println("\nPASSENGERS OF BOOKING " + booking.getBookingId());
        Passenger[] passengers = booking.getPassengers();
        for (int i = 0; i < passengers.length; i++) {
            User person = passengers[i];
            System.out.println("  " + (i + 1) + ". " + person.getDetails());
        }
    }

    public void displayAllPassengers() {
        System.out.println("\nALL PASSENGERS");
        Booking[] bookings = bookingService.getBookings();
        int count = 0;

        for (int i = 0; i < bookingService.getBookingCount(); i++) {
            Passenger[] passengers = bookings[i].getPassengers();
            for (int j = 0; j < passengers.length; j++) {
                User person = passengers[j];
                System.out.println("  Booking " + bookings[i].getBookingId() + " | " + person.getDetails());
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No passengers registered yet. Passengers are added when you book a ticket.");
        } else {
            System.out.println("Total passengers: " + count);
        }
    }

    // ------------------------------------------------------------ statistics

    public void displayStatistics() {
        Booking[] bookings = bookingService.getBookings();
        int totalBookings = bookingService.getBookingCount();

        int confirmed = 0;
        int pending = 0;
        int cancelled = 0;
        double revenue = 0;

        for (int i = 0; i < totalBookings; i++) {
            Booking booking = bookings[i];
            if (booking.getStatus().equals(Ticket.STATUS_CONFIRMED)) {
                confirmed++;
            } else if (booking.getStatus().equals(Ticket.STATUS_PENDING)) {
                pending++;
            } else {
                cancelled++;
            }

            Payment payment = booking.getPayment();
            if (payment != null && payment.isSuccessful()) {
                revenue = revenue + payment.getAmount() - payment.getRefundedAmount();
            }
        }

        int totalTickets = ticketService.getTicketCount();
        Ticket[] tickets = ticketService.getTickets();
        int activeTickets = 0;
        for (int i = 0; i < totalTickets; i++) {
            if (!tickets[i].isCancelled()) {
                activeTickets++;
            }
        }

        System.out.println("\nBOOKING STATISTICS");
        System.out.println("---------------------------------------------");
        System.out.printf(" Total bookings        : %d%n", totalBookings);
        System.out.printf("   Confirmed           : %d%n", confirmed);
        System.out.printf("   Pending (unpaid)    : %d%n", pending);
        System.out.printf("   Cancelled           : %d%n", cancelled);
        System.out.printf(" Total tickets issued  : %d%n", totalTickets);
        System.out.printf("   Active tickets      : %d%n", activeTickets);
        System.out.printf("   Cancelled tickets   : %d%n", totalTickets - activeTickets);
        System.out.printf(" Payment attempts      : %d%n", bookingService.getPaymentAttempts());
        System.out.printf(" Revenue (after refunds): Rs. %.2f%n", revenue);

        if (totalBookings > 0) {
            // Double wrapper object holding the average booking value
            Double average = Double.valueOf(revenue / totalBookings);
            System.out.printf(" Average per booking   : Rs. %.2f%n", average);
        }
    }
}
