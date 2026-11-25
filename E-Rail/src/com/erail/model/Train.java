package com.erail.model;

import com.erail.interfaces.Displayable;
import com.erail.interfaces.Reservable;

/**
 * A train: number, name, route, timings, base fare and its coaches.
 * Total/available seats are added up from the coaches, so they are always correct.
 * Implements Reservable (seat counts) and Displayable (table row).
 */
public class Train implements Reservable, Displayable {

    private int trainNumber;
    private String trainName;
    private Route route;
    private String departureTime;
    private String arrivalTime;
    private double baseFare;                   // fare for a Sleeper seat for the whole journey
    private Coach[] coaches;

    // Constructor 1: the coaches are supplied by the caller
    public Train(int trainNumber, String trainName, Route route,
                 String departureTime, double baseFare, Coach[] coaches) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = route.calculateArrival(departureTime);
        this.baseFare = baseFare;
        this.coaches = coaches;
    }

    // Constructor 2 (overloading): the train gets the standard set of three coaches
    public Train(int trainNumber, String trainName, Route route,
                 String departureTime, double baseFare) {
        this(trainNumber, trainName, route, departureTime, baseFare, createDefaultCoaches());
    }

    // static method: needs no Train object, so it may be used inside this(...)
    private static Coach[] createDefaultCoaches() {
        Coach[] defaultCoaches = new Coach[3];
        defaultCoaches[0] = new Coach("S1", Coach.TYPE_SLEEPER, 5, 4);   // 20 seats
        defaultCoaches[1] = new Coach("B1", Coach.TYPE_AC3);             // 16 seats
        defaultCoaches[2] = new Coach("A1", Coach.TYPE_AC2, 3, 4);       // 12 seats
        return defaultCoaches;
    }

    // Reservable: add up the seats of every coach
    @Override
    public int getTotalSeats() {
        int total = 0;
        for (int i = 0; i < coaches.length; i++) {
            total = total + coaches[i].getTotalSeats();
        }
        return total;
    }

    @Override
    public int getAvailableSeats() {
        int available = 0;
        for (int i = 0; i < coaches.length; i++) {
            available = available + coaches[i].getAvailableSeats();
        }
        return available;
    }

    @Override
    public boolean hasAvailableSeats() {
        return getAvailableSeats() > 0;
    }

    public static String getTableHeader() {
        return String.format("%-6s %-20s %-10s %-10s %-6s %-12s %5s %10s",
                "No.", "Train Name", "From", "To", "Dep", "Arr", "Seats", "Base Fare");
    }

    // Displayable: one table row
    @Override
    public String getSummary() {
        return String.format("%-6d %-20s %-10s %-10s %-6s %-12s %5d %10.2f",
                trainNumber, trainName, getSourceCity(), getDestinationCity(),
                departureTime, arrivalTime, getAvailableSeats(), baseFare);
    }

    public String getSourceCity() {
        return route.getSource().getCity();
    }

    public String getDestinationCity() {
        return route.getDestination().getCity();
    }

    public Coach getCoach(int index) {
        return coaches[index];
    }

    public int getCoachCount() {
        return coaches.length;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public Route getRoute() {
        return route;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public double getBaseFare() {
        return baseFare;
    }
}
