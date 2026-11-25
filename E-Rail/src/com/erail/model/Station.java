package com.erail.model;

import com.erail.interfaces.Displayable;

/**
 * A railway station (ID, name, short code and city).
 * Routes are built from two Stations, so searching a route or train can reuse
 * the matches() method below.
 */
public class Station implements Displayable {

    private int stationId;
    private String name;
    private String code;
    private String city;

    public Station(int stationId, String name, String code, String city) {
        this.stationId = stationId;
        this.name = name;
        this.code = code.toUpperCase();        // codes are always stored in capitals
        this.city = city;
    }

    // true if the text is this station's code, name or city (ignoring upper/lower case)
    public boolean matches(String text) {
        String search = text.trim();
        return code.equalsIgnoreCase(search)
                || name.equalsIgnoreCase(search)
                || city.equalsIgnoreCase(search);
    }

    public static String getTableHeader() {
        return String.format("%-4s %-6s %-20s %-12s", "ID", "Code", "Station Name", "City");
    }

    @Override
    public String getSummary() {
        return String.format("%-4d %-6s %-20s %-12s", stationId, code, name, city);
    }

    public int getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getCity() {
        return city;
    }
}
