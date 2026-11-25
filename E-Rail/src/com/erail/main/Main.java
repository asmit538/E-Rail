package com.erail.main;

import com.erail.exceptions.InvalidPNRException;
import com.erail.exceptions.InvalidPassengerException;
import com.erail.exceptions.SeatNotAvailableException;
import com.erail.exceptions.TrainNotFoundException;
import com.erail.model.Booking;
import com.erail.model.Coach;
import com.erail.model.Passenger;
import com.erail.model.Payment;
import com.erail.model.Route;
import com.erail.model.Ticket;
import com.erail.model.Train;
import com.erail.service.BookingService;
import com.erail.service.RailwaySystem;
import com.erail.service.TrainService;
import com.erail.validation.InputValidator;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entry point and the whole console screen: menus, prompts and messages.
 * Main does not contain business rules. It asks the user for input and calls
 * RailwaySystem / the services, then catches their exceptions and shows a
 * friendly message. Nothing the user types should ever crash the program.
 */
public class Main {

    // static: there is exactly one Scanner and one RailwaySystem for the whole program
    private static final Scanner scanner = new Scanner(System.in);
    private static RailwaySystem railwaySystem;

    public static void main(String[] args) {
        railwaySystem = new RailwaySystem();
        boolean running = true;

        try {
            while (running) {
                showMainMenu();
                int choice = readMenuChoice(11);
                try {
                    running = handleMainMenuChoice(choice);
                } catch (NoSuchElementException e) {
                    throw e;                   // input ended: let the outer catch handle it
                } catch (RuntimeException e) {
                    // Safety net: any unexpected problem is reported, the program carries on
                    System.out.println("Something went wrong: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            // Happens when the input stream ends (for example Ctrl+D, or a test file ends)
            System.out.println("\nNo more input. Closing E-Rail.");
        } finally {
            // finally always runs: close the Scanner whichever way the loop ended
            scanner.close();
            System.out.println("Thank you for using E-Rail. Goodbye!");
        }
    }

    // ================================================================ menus

    private static void showMainMenu() {
        System.out.println();
        System.out.println("=============================================");
        System.out.println("          E-RAIL RAILWAY RESERVATION");
        System.out.println("=============================================");
        System.out.println();
        System.out.println("1. Train Management");
        System.out.println("2. Passenger Management");
        System.out.println("3. Search Trains");
        System.out.println("4. Search Routes");
        System.out.println("5. Book Ticket");
        System.out.println("6. Make Payment");
        System.out.println("7. Check PNR Status");
        System.out.println("8. Cancel Ticket");
        System.out.println("9. View All Bookings");
        System.out.println("10. Booking Reports");
        System.out.println("11. Exit");
        System.out.println();
        System.out.println("=============================================");
        System.out.println();
    }

    // Returns false when the user chooses Exit
    private static boolean handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1:
                trainManagementMenu();
                break;
            case 2:
                passengerManagementMenu();
                break;
            case 3:
                searchTrainsMenu();
                break;
            case 4:
                searchRoutesMenu();
                break;
            case 5:
                bookTicket();
                break;
            case 6:
                makePaymentFromMenu();
                break;
            case 7:
                checkPnrStatus();
                break;
            case 8:
                cancelTicket();
                break;
            case 9:
                railwaySystem.getReportService().displayAllBookings();
                break;
            case 10:
                reportsMenu();
                break;
            case 11:
                return false;
            default:
                break;                         // cannot happen: readMenuChoice only returns 1-11
        }
        return true;
    }

    private static void trainManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- TRAIN MANAGEMENT ---");
            System.out.println("1. Display all trains");
            System.out.println("2. Train details (timings, fares, seats)");
            System.out.println("3. Check seat availability and seat map");
            System.out.println("4. Display all stations");
            System.out.println("5. Back to main menu");
            int choice = readMenuChoice(5);

            switch (choice) {
                case 1:
                    railwaySystem.getTrainService().displayAllTrains();
                    break;
                case 2:
                    showTrainDetailsByNumber();
                    break;
                case 3:
                    showSeatAvailability();
                    break;
                case 4:
                    railwaySystem.getTrainService().displayAllStations();
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    private static void passengerManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- PASSENGER MANAGEMENT ---");
            System.out.println("1. View passengers of a booking");
            System.out.println("2. View all registered passengers");
            System.out.println("3. Back to main menu");
            int choice = readMenuChoice(3);

            switch (choice) {
                case 1:
                    viewPassengersOfBooking();
                    break;
                case 2:
                    railwaySystem.getReportService().displayAllPassengers();
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    private static void searchTrainsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- SEARCH TRAINS ---");
            System.out.println("1. Search by source and destination");
            System.out.println("2. Search by train number");
            System.out.println("3. Back to main menu");
            int choice = readMenuChoice(3);

            switch (choice) {
                case 1:
                    searchTrainsBySourceAndDestination();
                    break;
                case 2:
                    showTrainDetailsByNumber();
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    private static void searchRoutesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- SEARCH ROUTES ---");
            System.out.println("1. Display all routes");
            System.out.println("2. Search routes between two stations");
            System.out.println("3. Back to main menu");
            int choice = readMenuChoice(3);

            switch (choice) {
                case 1:
                    railwaySystem.getTrainService().displayAllRoutes();
                    break;
                case 2:
                    searchRoutesBetweenStations();
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- BOOKING REPORTS ---");
            System.out.println("1. All bookings");
            System.out.println("2. Confirmed bookings");
            System.out.println("3. Cancelled bookings");
            System.out.println("4. Pending (unpaid) bookings");
            System.out.println("5. Booking statistics");
            System.out.println("6. Back to main menu");
            int choice = readMenuChoice(6);

            switch (choice) {
                case 1:
                    railwaySystem.getReportService().displayAllBookings();
                    break;
                case 2:
                    railwaySystem.getReportService().displayConfirmedBookings();
                    break;
                case 3:
                    railwaySystem.getReportService().displayCancelledBookings();
                    break;
                case 4:
                    railwaySystem.getReportService().displayPendingBookings();
                    break;
                case 5:
                    railwaySystem.getReportService().displayStatistics();
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    // ================================================================ train and route features

    private static void showTrainDetailsByNumber() {
        String text = readLine("Enter train number (for example 10101): ");
        try {
            Train train = railwaySystem.findTrain(text);
            railwaySystem.getTrainService().displayTrainDetails(train);
        } catch (TrainNotFoundException e) {
            System.out.println("Not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid train number: " + e.getMessage());
        }
    }

    private static void showSeatAvailability() {
        String text = readLine("Enter train number (for example 10101): ");
        try {
            Train train = railwaySystem.findTrain(text);
            railwaySystem.getTrainService().checkSeatAvailability(train);

            int coachChoice = readInt("\nEnter coach number 1-" + train.getCoachCount()
                    + " to see its seat map (0 to skip): ", 0, train.getCoachCount(), "Coach number");
            if (coachChoice > 0) {
                train.getCoach(coachChoice - 1).displaySeatMap();
            }
        } catch (TrainNotFoundException e) {
            System.out.println("Not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid train number: " + e.getMessage());
        }
    }

    private static void searchTrainsBySourceAndDestination() {
        String source = readLine("Enter source (station code, name or city): ");
        String destination = readLine("Enter destination (station code, name or city): ");
        try {
            Train[] results = railwaySystem.searchTrains(source, destination);
            System.out.println("\n" + results.length + " train(s) found:");
            railwaySystem.getTrainService().displayTrains(results);
        } catch (TrainNotFoundException e) {
            System.out.println("No result: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private static void searchRoutesBetweenStations() {
        String source = readLine("Enter source (station code, name or city): ");
        String destination = readLine("Enter destination (station code, name or city): ");
        try {
            Route[] results = railwaySystem.searchRoutes(source, destination);
            if (results.length == 0) {
                System.out.println("No route found from " + source.trim() + " to " + destination.trim() + ".");
            } else {
                System.out.println("\n" + results.length + " route(s) found:");
                railwaySystem.getTrainService().displayRoutes(results);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private static void viewPassengersOfBooking() {
        try {
            int bookingId = readInt("Enter booking ID: ", 1, 999999, "Booking ID");
            Booking booking = railwaySystem.getBookingService().findBookingById(bookingId);
            railwaySystem.getReportService().displayPassengersOfBooking(booking);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================================================================ booking

    private static void bookTicket() {
        System.out.println("\n--- BOOK TICKET ---");
        TrainService trainService = railwaySystem.getTrainService();
        BookingService bookingService = railwaySystem.getBookingService();

        try {
            trainService.displayAllTrains();
            String trainText = readLine("\nEnter train number to book (0 to go back): ");
            if (trainText.trim().equals("0")) {
                return;
            }

            Train train = railwaySystem.findTrain(trainText);       // may throw
            trainService.displayTrainDetails(train);

            // Is there at least one free seat? (throws SeatNotAvailableException if not)
            bookingService.validateBookingRequest(train, 1);

            int count = readInt("\nNumber of passengers (1-" + BookingService.MAX_PASSENGERS_PER_BOOKING
                    + "): ", 1, BookingService.MAX_PASSENGERS_PER_BOOKING, "Number of passengers");
            bookingService.validateBookingRequest(train, count);     // enough seats for everybody?

            // Arrays that hold one entry per passenger
            Passenger[] passengers = new Passenger[count];
            Coach[] chosenCoaches = new Coach[count];
            int[] chosenSeats = new int[count];

            for (int i = 0; i < count; i++) {
                System.out.println("\n--- Passenger " + (i + 1) + " of " + count + " ---");
                passengers[i] = readPassenger();
                boolean selected = selectCoachAndSeat(train, passengers[i], i, chosenCoaches, chosenSeats);
                if (!selected) {
                    System.out.println("Booking cancelled. No seats were reserved.");
                    return;
                }
            }

            Booking booking = railwaySystem.bookTicket(train, passengers, chosenCoaches, chosenSeats, count);

            System.out.println("\nSeats reserved. Your booking is created but NOT confirmed until payment is made.");
            railwaySystem.getReportService().displayBookingDetails(booking);

            if (readYesNo("\nPay now? (Y/N): ")) {
                payForBooking(booking);
            } else {
                System.out.println("You can pay later: choose option 6 and enter Booking ID "
                        + booking.getBookingId() + ".");
            }
        } catch (TrainNotFoundException | SeatNotAvailableException e) {
            // Multi-catch: two unrelated checked exceptions handled the same way
            System.out.println("Booking not possible: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Multi-catch: two unrelated unchecked exceptions
            System.out.println("Invalid booking request: " + e.getMessage());
        }
    }

    // Lets the user pick a coach and a seat for one passenger.
    // Stores the choice in the arrays at position 'index'. Returns false if the user cancels.
    private static boolean selectCoachAndSeat(Train train, Passenger passenger, int index,
                                              Coach[] chosenCoaches, int[] chosenSeats) {
        TrainService trainService = railwaySystem.getTrainService();

        while (true) {
            trainService.displayCoachList(train, passenger);
            int coachChoice = readInt("Select coach number 1-" + train.getCoachCount()
                    + " (0 to cancel the booking): ", 0, train.getCoachCount(), "Coach number");
            if (coachChoice == 0) {
                return false;
            }

            Coach coach = train.getCoach(coachChoice - 1);
            if (!coach.hasAvailableSeats()) {
                System.out.println("Coach " + coach.getCoachNumber() + " is full. Please choose another coach.");
                continue;
            }

            coach.displaySeatMap();
            int seatNumber = readInt("Enter seat number (0 to choose another coach): ",
                    0, coach.getTotalSeats(), "Seat number");
            if (seatNumber == 0) {
                continue;
            }

            try {
                railwaySystem.getBookingService().checkSeatChoice(
                        coach, seatNumber, chosenCoaches, chosenSeats, index);
                chosenCoaches[index] = coach;
                chosenSeats[index] = seatNumber;
                System.out.println("Seat " + seatNumber + " in coach " + coach.getCoachNumber() + " selected.");
                return true;
            } catch (SeatNotAvailableException e) {
                System.out.println("Cannot select that seat: " + e.getMessage());
            }
        }
    }

    // Reads name, age and gender; each field is repeated until it is valid
    private static Passenger readPassenger() {
        String name = readValidName();
        int age = readValidAge();
        String gender = readValidGender();
        return new Passenger(name, age, gender);
    }

    private static String readValidName() {
        while (true) {
            String text = readLine("  Name   : ");
            try {
                return InputValidator.validateName(text);
            } catch (InvalidPassengerException e) {
                System.out.println("  Invalid name: " + e.getMessage());
            }
        }
    }

    private static int readValidAge() {
        while (true) {
            String text = readLine("  Age    : ");
            try {
                return InputValidator.validateAge(text);
            } catch (InvalidPassengerException e) {
                System.out.println("  Invalid age: " + e.getMessage());
            }
        }
    }

    private static String readValidGender() {
        while (true) {
            String text = readLine("  Gender (M/F/O): ");
            try {
                return InputValidator.validateGender(text);
            } catch (InvalidPassengerException e) {
                System.out.println("  Invalid gender: " + e.getMessage());
            }
        }
    }

    // ================================================================ payment

    private static void makePaymentFromMenu() {
        System.out.println("\n--- MAKE PAYMENT ---");
        try {
            int bookingId = readInt("Enter booking ID: ", 1, 999999, "Booking ID");
            Booking booking = railwaySystem.getBookingService().findBookingById(bookingId);
            railwaySystem.getReportService().displayBookingDetails(booking);
            payForBooking(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Payment not possible: " + e.getMessage());
        }
    }

    // Used both right after booking and from the menu
    private static void payForBooking(Booking booking) {
        BookingService bookingService = railwaySystem.getBookingService();

        try {
            bookingService.checkPayable(booking);      // throws IllegalStateException if paid / cancelled

            System.out.printf("%nAmount payable: Rs. %.2f%n", bookingService.getPayableAmount(booking));
            System.out.println("1. Cash");
            System.out.println("2. UPI");
            System.out.println("3. Card");
            int choice = readMenuChoice(3);

            String mode;
            String reference = "";
            if (choice == 1) {
                mode = Payment.MODE_CASH;
            } else if (choice == 2) {
                mode = Payment.MODE_UPI;
                reference = readLine("Enter UPI ID (for example name@bank): ");
            } else {
                mode = Payment.MODE_CARD;
                reference = readLine("Enter 16-digit card number: ");
            }

            Payment payment = railwaySystem.makePayment(booking, mode, reference);
            System.out.println("\nPAYMENT RESULT");
            System.out.println(payment.getDetails());

            if (payment.isSuccessful()) {
                System.out.println("\nPayment successful. Booking " + booking.getBookingId() + " is CONFIRMED.");
                Ticket[] tickets = booking.getTickets();
                for (int i = 0; i < tickets.length; i++) {
                    if (!tickets[i].isCancelled()) {
                        railwaySystem.getTicketService().displayTicket(tickets[i]);
                    }
                }
            } else {
                System.out.println("\nPayment failed. The booking stays PENDING and the seats stay reserved.");
                System.out.println("Choose option 6 from the main menu to try again.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Payment not possible: " + e.getMessage());
        } finally {
            System.out.println("(Payment session closed)");
        }
    }

    // ================================================================ PNR and cancellation

    private static void checkPnrStatus() {
        System.out.println("\n--- PNR STATUS ---");
        String pnr = readLine("Enter PNR (for example PNR100001): ");
        try {
            railwaySystem.getTicketService().checkPnrStatus(pnr);
        } catch (InvalidPNRException e) {
            System.out.println("Invalid PNR: " + e.getMessage());
        }
    }

    private static void cancelTicket() {
        System.out.println("\n--- CANCEL TICKET ---");
        String pnr = readLine("Enter PNR of the ticket to cancel: ");
        try {
            Ticket ticket = railwaySystem.getTicketService().findTicketByPnr(pnr);
            railwaySystem.getBookingService().checkCancellable(ticket);   // stop early if already cancelled
            railwaySystem.getTicketService().displayTicket(ticket);

            if (!readYesNo("\nDo you really want to cancel this ticket? (Y/N): ")) {
                System.out.println("Cancellation aborted. Your ticket is unchanged.");
                return;
            }

            Ticket cancelled = railwaySystem.cancelTicket(pnr);
            System.out.println("\nTicket " + cancelled.getPnr() + " has been CANCELLED.");
            System.out.println("Seat " + cancelled.getSeat().getSeatNumber() + " in coach "
                    + cancelled.getCoach().getCoachNumber() + " is available again.");

            Booking booking = railwaySystem.getBookingService().findBookingById(cancelled.getBookingId());
            if (booking.isPaid()) {
                System.out.printf("Refund of Rs. %.2f will be returned to the original payment mode (simulated).%n",
                        cancelled.getFare());
            } else {
                System.out.println("No payment had been made, so no refund is due.");
            }
        } catch (InvalidPNRException e) {
            System.out.println("Invalid PNR: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Cannot cancel: " + e.getMessage());
        }
    }

    // ================================================================ input helpers

    // Prints a prompt and returns the whole line the user typed
    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    // Keeps asking until the user types a whole number from min to max
    private static int readInt(String prompt, int min, int max, String fieldName) {
        while (true) {
            String text = readLine(prompt);
            try {
                return InputValidator.parseInteger(text, min, max, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    // Menu choices: 1 to maxChoice. A wrong choice shows a message and asks again.
    private static int readMenuChoice(int maxChoice) {
        while (true) {
            String text = readLine("Enter your choice: ");
            try {
                return InputValidator.parseInteger(text, 1, maxChoice, "Menu choice");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid choice: " + e.getMessage());
            }
        }
    }

    // Keeps asking until the user types Y or N (any letter case)
    private static boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).trim();
            if (answer.equalsIgnoreCase("Y") || answer.equalsIgnoreCase("YES")) {
                return true;
            } else if (answer.equalsIgnoreCase("N") || answer.equalsIgnoreCase("NO")) {
                return false;
            }
            System.out.println("Please type Y or N.");
        }
    }
}
