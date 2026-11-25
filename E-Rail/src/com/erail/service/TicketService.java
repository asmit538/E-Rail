package com.erail.service;

import com.erail.exceptions.InvalidPNRException;
import com.erail.model.Ticket;
import com.erail.validation.InputValidator;

/**
 * Keeps every ticket in an array and answers PNR questions.
 * It also generates PNR numbers with a static counter.
 */
public class TicketService {

    public static final int MAX_TICKETS = 400;

    // static: one counter shared by the whole program, so no two PNRs are equal
    private static int nextPnrNumber = 100001;

    private Ticket[] tickets = new Ticket[MAX_TICKETS];
    private int ticketCount = 0;

    // Creates the next PNR, e.g. "PNR100001", "PNR100002", ...
    public String generatePnr() {
        String pnr = "PNR" + nextPnrNumber;
        nextPnrNumber++;
        return pnr;
    }

    public void addTicket(Ticket ticket) {
        if (ticketCount >= tickets.length) {
            throw new IllegalStateException("Ticket storage is full.");
        }
        tickets[ticketCount] = ticket;
        ticketCount++;
    }

    // Linear search of the array. Throws InvalidPNRException if the format is wrong
    // (thrown by the validator and passed up) or if no ticket has this PNR.
    public Ticket findTicketByPnr(String pnrText) throws InvalidPNRException {
        String pnr = InputValidator.validatePnr(pnrText);

        for (int i = 0; i < ticketCount; i++) {
            if (tickets[i].getPnr().equals(pnr)) {
                return tickets[i];
            }
        }
        throw new InvalidPNRException("No ticket found for PNR " + pnr + ".");
    }

    // Prints the full ticket
    public void displayTicket(Ticket ticket) {
        System.out.println(ticket.getTicketDetails());
    }

    // PNR status: find the ticket and print it
    public void checkPnrStatus(String pnrText) throws InvalidPNRException {
        Ticket ticket = findTicketByPnr(pnrText);
        displayTicket(ticket);
    }

    public Ticket[] getTickets() {
        return tickets;
    }

    public int getTicketCount() {
        return ticketCount;
    }
}
