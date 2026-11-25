package com.erail.service;

import com.erail.exceptions.TrainNotFoundException;
import com.erail.interfaces.Displayable;
import com.erail.interfaces.Reservable;
import com.erail.model.Coach;
import com.erail.model.Passenger;
import com.erail.model.Route;
import com.erail.model.Station;
import com.erail.model.Train;

/**
 * Everything about the railway network that can be looked up or displayed:
 * trains, stations and routes. It does not change any data.
 */
public class TrainService {

    private Station[] stations;
    private Route[] routes;
    private Train[] trains;
    private FareCalculator fareCalculator;

    public TrainService(Station[] stations, Route[] routes, Train[] trains,
                        FareCalculator fareCalculator) {
        this.stations = stations;
        this.routes = routes;
        this.trains = trains;
        this.fareCalculator = fareCalculator;
    }

    // ------------------------------------------------------------ trains

    public void displayAllTrains() {
        System.out.println("\nAVAILABLE TRAINS");
        displayTrains(trains);
    }

    // Prints a table for any array of trains (all trains or search results)
    public void displayTrains(Train[] list) {
        System.out.println(Train.getTableHeader());
        System.out.println("-----------------------------------------------------------------------------------");
        printSummaries(list, list.length);
        System.out.println("(Seats = free seats now. Base Fare = Sleeper fare for the full journey.)");
    }

    // Finds all trains running from 'source' to 'destination'.
    // Source/destination may be a station code, name or city.
    public Train[] searchTrains(String source, String destination) throws TrainNotFoundException {
        Train[] temp = new Train[trains.length];
        int matchCount = 0;

        for (int i = 0; i < trains.length; i++) {
            if (trains[i].getRoute().connects(source, destination)) {
                temp[matchCount] = trains[i];
                matchCount++;
            }
        }

        if (matchCount == 0) {
            throw new TrainNotFoundException(
                    "No trains found from " + source + " to " + destination + ".");
        }

        // Copy into an array of exactly the right size
        Train[] result = new Train[matchCount];
        for (int i = 0; i < matchCount; i++) {
            result[i] = temp[i];
        }
        return result;
    }

    public Train findTrainByNumber(int trainNumber) throws TrainNotFoundException {
        for (int i = 0; i < trains.length; i++) {
            if (trains[i].getTrainNumber() == trainNumber) {
                return trains[i];
            }
        }
        throw new TrainNotFoundException("No train found with number " + trainNumber + ".");
    }

    // Timings, journey information, fares and seat availability of one train
    public void displayTrainDetails(Train train) {
        Route route = train.getRoute();
        System.out.println("\nTRAIN " + train.getTrainNumber() + " - " + train.getTrainName());
        System.out.println("  Route      : " + route.getSource().getName()
                + " -> " + route.getDestination().getName());
        System.out.println("  Departure  : " + train.getDepartureTime());
        System.out.println("  Arrival    : " + train.getArrivalTime());
        System.out.println("  Distance   : " + route.getDistanceKm() + " km");
        System.out.println("  Duration   : " + route.getDurationText());

        System.out.println("\n  Fare per passenger (adult):");
        for (int i = 0; i < train.getCoachCount(); i++) {
            Coach coach = train.getCoach(i);
            double fare = fareCalculator.calculateFare(train.getBaseFare(), coach.getCoachType());
            System.out.printf("    %-10s %-12s Rs. %8.2f%n",
                    coach.getCoachNumber(), coach.getCoachType(), fare);
        }
        System.out.printf("  Discounts: children up to %d years %.0f%% off, seniors %d+ years %.0f%% off%n",
                Passenger.CHILD_MAX_AGE, FareCalculator.CHILD_DISCOUNT * 100,
                Passenger.SENIOR_MIN_AGE, FareCalculator.SENIOR_DISCOUNT * 100);

        checkSeatAvailability(train);
    }

    // Seat availability of the whole train and each coach
    public void checkSeatAvailability(Train train) {
        System.out.println("\n  Seat availability:");
        printAvailability(train, "Whole train");
        for (int i = 0; i < train.getCoachCount(); i++) {
            Coach coach = train.getCoach(i);
            printAvailability(coach, "Coach " + coach.getCoachNumber() + " (" + coach.getCoachType() + ")");
        }
    }

    // Runtime polymorphism: 'item' may be a Train or a Coach.
    // Java decides at run time which getAvailableSeats() to call.
    private void printAvailability(Reservable item, String label) {
        System.out.printf("    %-28s : %3d of %3d seats free%n",
                label, item.getAvailableSeats(), item.getTotalSeats());
    }

    // Lists the coaches of a train with the fare for ONE passenger (used while booking)
    public void displayCoachList(Train train, Passenger passenger) {
        System.out.println("\nCoaches on train " + train.getTrainNumber() + ":");
        System.out.printf("  %-4s %-8s %-12s %-6s %12s%n", "No.", "Coach", "Type", "Free", "Your fare");
        for (int i = 0; i < train.getCoachCount(); i++) {
            Coach coach = train.getCoach(i);
            double fare = fareCalculator.calculateFare(train, coach, passenger);
            System.out.printf("  %-4d %-8s %-12s %-6d Rs. %8.2f%n",
                    i + 1, coach.getCoachNumber(), coach.getCoachType(),
                    coach.getAvailableSeats(), fare);
        }
    }

    // ------------------------------------------------------------ stations and routes

    public void displayAllStations() {
        System.out.println("\nSTATIONS");
        System.out.println(Station.getTableHeader());
        System.out.println("------------------------------------------");
        printSummaries(stations, stations.length);
    }

    public void displayAllRoutes() {
        System.out.println("\nAVAILABLE ROUTES");
        displayRoutes(routes);
    }

    public void displayRoutes(Route[] list) {
        System.out.println(Route.getTableHeader());
        System.out.println("---------------------------------------------------------------------");
        printSummaries(list, list.length);
    }

    // Returns the routes between two stations (the array is empty if there is none)
    public Route[] searchRoutes(String source, String destination) {
        Route[] temp = new Route[routes.length];
        int matchCount = 0;

        for (int i = 0; i < routes.length; i++) {
            if (routes[i].connects(source, destination)) {
                temp[matchCount] = routes[i];
                matchCount++;
            }
        }

        Route[] result = new Route[matchCount];
        for (int i = 0; i < matchCount; i++) {
            result[i] = temp[i];
        }
        return result;
    }

    // Runtime polymorphism again: the array may hold Trains, Stations or Routes,
    // and each object's own getSummary() is called.
    private void printSummaries(Displayable[] items, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println(items[i].getSummary());
        }
    }
}
