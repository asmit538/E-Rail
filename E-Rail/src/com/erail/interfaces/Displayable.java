package com.erail.interfaces;

/**
 * Anything that can describe itself as ONE formatted line of text (a table row).
 * Implemented by Station, Route, Train and Ticket, so one method in TrainService
 * can print a table of any of them.
 */
public interface Displayable {

    // Returns one formatted row for a console table
    String getSummary();
}
