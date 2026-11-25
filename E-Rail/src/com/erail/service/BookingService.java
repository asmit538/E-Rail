package com.erail.service;

import com.erail.exceptions.InvalidPNRException;
import com.erail.exceptions.SeatNotAvailableException;
import com.erail.model.Booking;
import com.erail.model.Coach;
import com.erail.model.Passenger;
import com.erail.model.Payment;
import com.erail.model.Seat;
import com.erail.model.Ticket;
import com.erail.model.Train;

/**
 * The heart of the system: validates booking requests, allocates seats,
 * creates tickets, handles payment and cancels tickets.
 * All bookings are stored in an array.
 */
public class BookingService {

    public static final int MAX_PASSENGERS_PER_BOOKING = 4;
    public static final int MAX_BOOKINGS = 100;

    private Booking[] bookings = new Booking[MAX_BOOKINGS];
    private int bookingCount = 0;
    private int paymentAttempts = 0;

    private TicketService ticketService;
    private FareCalculator fareCalculator;

    public BookingService(TicketService ticketService, FareCalculator fareCalculator) {
        this.ticketService = ticketService;
        this.fareCalculator = fareCalculator;
    }

    // ------------------------------------------------------------ validation

    // Checks that a booking for 'passengerCount' people is possible on this train.
    // Throws SeatNotAvailableException (checked) when there are not enough free seats.
    public void validateBookingRequest(Train train, int passengerCount)
            throws SeatNotAvailableException {
        if (train == null) {
            throw new IllegalArgumentException("No train selected.");
        }
        if (passengerCount < 1 || passengerCount > MAX_PASSENGERS_PER_BOOKING) {
            throw new IllegalArgumentException("You can book 1 to "
                    + MAX_PASSENGERS_PER_BOOKING + " passengers at a time.");
        }
        if (bookingCount >= bookings.length) {
            throw new IllegalStateException("Booking limit reached. No more bookings can be stored.");
        }
        if (!train.hasAvailableSeats()) {
            throw new SeatNotAvailableException(
                    "Train " + train.getTrainNumber() + " is fully booked. No seats available.");
        }
        if (train.getAvailableSeats() < passengerCount) {
            throw new SeatNotAvailableException("Only " + train.getAvailableSeats()
                    + " seat(s) available on train " + train.getTrainNumber()
                    + ", but " + passengerCount + " were requested.");
        }
    }

    // Checks one seat choice BEFORE booking. The seat must exist, be free and not
    // already chosen by an earlier passenger of the same booking.
    public void checkSeatChoice(Coach coach, int seatNumber, Coach[] chosenCoaches,
                                int[] chosenSeats, int chosenCount)
            throws SeatNotAvailableException {
        Seat seat = coach.findSeat(seatNumber);
        if (seat == null) {
            throw new SeatNotAvailableException(
                    "Seat " + seatNumber + " does not exist in coach " + coach.getCoachNumber() + ".");
        }
        if (!seat.isAvailable()) {
            throw new SeatNotAvailableException("Seat " + seatNumber + " is already booked.");
        }
        for (int i = 0; i < chosenCount; i++) {
            // == compares the two Coach objects themselves (are they the same coach?)
            if (chosenCoaches[i] == coach && chosenSeats[i] == seatNumber) {
                throw new SeatNotAvailableException("Seat " + seatNumber
                        + " is already selected for passenger " + (i + 1) + ".");
            }
        }
    }

    // ------------------------------------------------------------ booking

    // Books the chosen seats, creates one Ticket per passenger and stores a Booking.
    // If any seat fails, the seats already taken are released (rollback) and the
    // exception is thrown again so the caller can report it.
    public Booking createBooking(Train train, Passenger[] passengers, Coach[] coaches,
                                 int[] seatNumbers, int count)
            throws SeatNotAvailableException {
        validateBookingRequest(train, count);

        Ticket[] newTickets = new Ticket[count];
        int ticketsCreated = 0;

        try {
            for (int i = 0; i < count; i++) {
                Seat seat = coaches[i].bookSeat(seatNumbers[i]);      // may throw
                double fare = fareCalculator.calculateFare(train, coaches[i], passengers[i]);
                String pnr = ticketService.generatePnr();
                newTickets[i] = new Ticket(pnr, passengers[i], train, coaches[i], seat, fare);
                ticketsCreated++;
            }
        } catch (SeatNotAvailableException e) {
            // Rollback: free every seat we already booked in this request
            for (int i = 0; i < ticketsCreated; i++) {
                newTickets[i].getSeat().release();
            }
            throw e;                                                   // pass the problem upwards
        }

        for (int i = 0; i < count; i++) {
            ticketService.addTicket(newTickets[i]);
        }

        Booking booking = new Booking(passengers, newTickets);
        bookings[bookingCount] = booking;
        bookingCount++;
        return booking;
    }

    public Booking findBookingById(int bookingId) {
        for (int i = 0; i < bookingCount; i++) {
            if (bookings[i].getBookingId() == bookingId) {
                return bookings[i];
            }
        }
        throw new IllegalArgumentException("No booking found with ID " + bookingId + ".");
    }

    // ------------------------------------------------------------ payment

    // A booking can be paid only if it is not cancelled and not already paid
    public void checkPayable(Booking booking) {
        if (booking.getStatus().equals(Ticket.STATUS_CANCELLED)) {
            throw new IllegalStateException(
                    "Booking " + booking.getBookingId() + " is cancelled. Payment is not allowed.");
        }
        if (booking.isPaid()) {
            throw new IllegalStateException(
                    "Booking " + booking.getBookingId() + " is already paid.");
        }
    }

    public double getPayableAmount(Booking booking) {
        return fareCalculator.calculateTotalAmount(booking.getTickets());
    }

    // Simulates a payment. On success every active ticket becomes CONFIRMED.
    // 'reference' is the UPI ID or card number (ignored for Cash).
    public Payment makePayment(Booking booking, String mode, String reference) {
        checkPayable(booking);

        Payment payment = new Payment(getPayableAmount(booking), mode);
        boolean success = false;
        try {
            success = payment.process(reference);
        } finally {
            // finally always runs, so every attempt is counted - success or not
            paymentAttempts++;
        }

        booking.setPayment(payment);               // keep the latest attempt (even a failed one)
        if (success) {
            Ticket[] tickets = booking.getTickets();
            for (int i = 0; i < tickets.length; i++) {
                if (!tickets[i].isCancelled()) {
                    tickets[i].setStatus(Ticket.STATUS_CONFIRMED);
                }
            }
        }
        booking.refreshStatus();
        return payment;
    }

    // ------------------------------------------------------------ cancellation

    // Throws IllegalStateException (unchecked) if the ticket is already cancelled
    public void checkCancellable(Ticket ticket) {
        if (ticket.isCancelled()) {
            throw new IllegalStateException(
                    "Ticket " + ticket.getPnr() + " is already cancelled.");
        }
    }

    // Cancels one ticket: frees the seat, updates the statuses and records a refund
    // if the booking was paid. Exceptions from the called methods travel up to Main.
    public Ticket cancelTicket(String pnrText) throws InvalidPNRException {
        Ticket ticket = ticketService.findTicketByPnr(pnrText);   // may throw InvalidPNRException
        checkCancellable(ticket);                                  // may throw IllegalStateException

        Booking booking = findBookingById(ticket.getBookingId());

        ticket.getSeat().release();                // seat is free again, so seat counts update automatically
        ticket.setStatus(Ticket.STATUS_CANCELLED);
        if (booking.isPaid()) {
            booking.getPayment().addRefund(ticket.getFare());
        }
        booking.refreshStatus();
        return ticket;
    }

    // ------------------------------------------------------------ getters

    public Booking[] getBookings() {
        return bookings;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public int getPaymentAttempts() {
        return paymentAttempts;
    }
}
