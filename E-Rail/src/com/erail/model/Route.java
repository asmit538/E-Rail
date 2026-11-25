package com.erail.model;

import com.erail.interfaces.Displayable;

/**
 * A one-way journey between two stations: distance and estimated duration.
 * Every Train owns a Route (object modelling: a Train HAS-A Route, and a Route
 * HAS two Stations).
 */
public class Route implements Displayable {

    // Used when the duration is not given: we assume an average speed
    private static final int AVERAGE_SPEED_KMPH = 55;

    private Station source;
    private Station destination;
    private int distanceKm;
    private int durationMinutes;

    // Constructor 1: the duration is given
    public Route(Station source, Station destination, int distanceKm, int durationMinutes) {
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
    }

    // Constructor 2 (overloading): the duration is estimated from the distance.
    // this(...) calls constructor 1, so we do not repeat the assignments.
    public Route(Station source, Station destination, int distanceKm) {
        this(source, destination, distanceKm, distanceKm * 60 / AVERAGE_SPEED_KMPH);
    }

    // true if this route goes from 'from' to 'to' (each can be a code, name or city)
    public boolean connects(String from, String to) {
        return source.matches(from) && destination.matches(to);
    }

    // Example: 990 minutes -> "16h 30m"
    public String getDurationText() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    // Adds the journey time to a departure time such as "16:30".
    // Returns e.g. "09:00 (+1d)" when the train arrives on a later day.
    public String calculateArrival(String departureTime) {
        // substring() cuts the hour and minute parts out of "HH:mm"
        String hourText = departureTime.substring(0, 2);
        String minuteText = departureTime.substring(3, 5);

        // Integer.parseInt() converts text to a number
        int departureMinutes = Integer.parseInt(hourText) * 60 + Integer.parseInt(minuteText);
        int totalMinutes = departureMinutes + durationMinutes;

        int extraDays = totalMinutes / (24 * 60);
        int minutesOfDay = totalMinutes % (24 * 60);

        String result = String.format("%02d:%02d", minutesOfDay / 60, minutesOfDay % 60);
        if (extraDays > 0) {
            result = result + " (+" + extraDays + "d)";
        }
        return result;
    }

    public static String getTableHeader() {
        return String.format("%-18s    %-18s %8s   %s", "From", "To", "Distance", "Duration");
    }

    @Override
    public String getSummary() {
        return String.format("%-18s -> %-18s %5d km   %s",
                source.getName(), destination.getName(), distanceKm, getDurationText());
    }

    public Station getSource() {
        return source;
    }

    public Station getDestination() {
        return destination;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
