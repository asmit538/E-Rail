package com.erail.service;

import com.erail.exceptions.InvalidPNRException;
import com.erail.exceptions.SeatNotAvailableException;
import com.erail.exceptions.TrainNotFoundException;
import com.erail.model.Booking;
import com.erail.model.Coach;
import com.erail.model.Passenger;
import com.erail.model.Payment;
import com.erail.model.Route;
import com.erail.model.Station;
import com.erail.model.Ticket;
import com.erail.model.Train;
import com.erail.validation.InputValidator;

/**
 * The control centre of the application.
 *  1. Creates the sample data (stations, routes, trains).
 *  2. Creates the services and connects them to each other.
 *  3. Offers methods that coordinate several classes (validator + service).
 * Main talks to RailwaySystem, and RailwaySystem talks to the services.
 */
public class RailwaySystem {

    private Station[] stations;
    private Route[] routes;
    private Train[] trains;

    private FareCalculator fareCalculator;
    private TrainService trainService;
    private TicketService ticketService;
    private BookingService bookingService;
    private ReportService reportService;

    public RailwaySystem() {
        stations = createStations();
        routes = createRoutes(stations);
        trains = createTrains(routes);

        // The order matters: a service can only be given objects that already exist
        fareCalculator = new FareCalculator();
        trainService = new TrainService(stations, routes, trains, fareCalculator);
        ticketService = new TicketService();
        bookingService = new BookingService(ticketService, fareCalculator);
        reportService = new ReportService(bookingService, ticketService, fareCalculator);
    }

    // ------------------------------------------------------------ sample data
    // All stations, routes, trains and fares are MADE-UP sample data.

    private Station[] createStations() {
        Station[] list = new Station[8];
        list[0] = new Station(1, "New Delhi", "NDL", "Delhi");
        list[1] = new Station(2, "Mumbai Central", "MUM", "Mumbai");
        list[2] = new Station(3, "Howrah", "KOL", "Kolkata");
        list[3] = new Station(4, "Chennai Central", "CHE", "Chennai");
        list[4] = new Station(5, "Bengaluru City", "BLR", "Bengaluru");
        list[5] = new Station(6, "Hyderabad Deccan", "HYD", "Hyderabad");
        list[6] = new Station(7, "Ahmedabad Junction", "AMD", "Ahmedabad");
        list[7] = new Station(8, "Jaipur Junction", "JAI", "Jaipur");
        return list;
    }

    private Route[] createRoutes(Station[] s) {
        Station delhi = s[0];
        Station mumbai = s[1];
        Station kolkata = s[2];
        Station chennai = s[3];
        Station bengaluru = s[4];
        Station hyderabad = s[5];
        Station ahmedabad = s[6];
        Station jaipur = s[7];

        Route[] list = new Route[12];
        // Four-argument constructor: distance AND duration (minutes) are given
        list[0] = new Route(delhi, mumbai, 1384, 990);
        list[1] = new Route(mumbai, delhi, 1384, 990);
        list[2] = new Route(delhi, kolkata, 1450, 1020);
        list[3] = new Route(kolkata, delhi, 1450, 1020);
        // Three-argument constructor: duration is estimated from the distance
        list[4] = new Route(delhi, jaipur, 308);
        list[5] = new Route(jaipur, delhi, 308);
        list[6] = new Route(mumbai, ahmedabad, 492);
        list[7] = new Route(ahmedabad, mumbai, 492);
        list[8] = new Route(chennai, bengaluru, 346);
        list[9] = new Route(bengaluru, chennai, 346);
        list[10] = new Route(bengaluru, hyderabad, 570, 540);
        list[11] = new Route(hyderabad, chennai, 705, 600);
        return list;
    }

    private Train[] createTrains(Route[] r) {
        Train[] list = new Train[12];
        // Five-argument constructor: the train gets the standard 3 coaches (48 seats)
        list[0] = new Train(10101, "Capital Express", r[0], "16:30", 850);
        list[1] = new Train(10102, "Gateway Express", r[1], "17:00", 850);
        list[2] = new Train(10201, "Eastern Mail", r[2], "20:15", 900);
        list[3] = new Train(10202, "Howrah Mail", r[3], "21:00", 900);
        list[4] = new Train(10301, "Pink City Express", r[4], "06:00", 250);
        list[5] = new Train(10302, "Rajputana Link", r[5], "17:45", 250);
        list[6] = new Train(10401, "Sabarmati Express", r[6], "22:10", 350);
        list[7] = new Train(10402, "Gujarat Shuttle", r[7], "06:15", 350);
        list[8] = new Train(10501, "Garden City Express", r[8], "07:30", 300);
        list[9] = new Train(10502, "Silicon Express", r[9], "15:00", 300);
        list[10] = new Train(10601, "Deccan Link", r[10], "21:30", 450);

        // Six-argument constructor: a tiny train with ONE coach of only 4 seats (2 x 2).
        // It makes it easy to test the "no seats available" situation.
        Coach[] miniCoaches = new Coach[1];
        miniCoaches[0] = new Coach("A1", Coach.TYPE_AC2, 2, 2);
        list[11] = new Train(10701, "Charminar Mini", r[11], "10:00", 400, miniCoaches);
        return list;
    }

    // ------------------------------------------------------------ coordination methods

    // Validates the typed train number, then looks the train up.
    // Exceptions: IllegalArgumentException (bad text) or TrainNotFoundException (no such train).
    public Train findTrain(String trainNumberText) throws TrainNotFoundException {
        int trainNumber = InputValidator.validateTrainNumber(trainNumberText);
        return trainService.findTrainByNumber(trainNumber);
    }

    public Train[] searchTrains(String source, String destination) throws TrainNotFoundException {
        InputValidator.validateSourceAndDestination(source, destination);
        return trainService.searchTrains(source.trim(), destination.trim());
    }

    public Route[] searchRoutes(String source, String destination) {
        InputValidator.validateSourceAndDestination(source, destination);
        return trainService.searchRoutes(source.trim(), destination.trim());
    }

    public Booking bookTicket(Train train, Passenger[] passengers, Coach[] coaches,
                              int[] seatNumbers, int count) throws SeatNotAvailableException {
        return bookingService.createBooking(train, passengers, coaches, seatNumbers, count);
    }

    public Payment makePayment(Booking booking, String mode, String reference) {
        return bookingService.makePayment(booking, mode, reference);
    }

    public Ticket cancelTicket(String pnr) throws InvalidPNRException {
        return bookingService.cancelTicket(pnr);
    }

    // ------------------------------------------------------------ getters for the services

    public TrainService getTrainService() {
        return trainService;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    public ReportService getReportService() {
        return reportService;
    }
}
