package com.erail.service;

import com.erail.model.Coach;
import com.erail.model.Passenger;
import com.erail.model.Ticket;
import com.erail.model.Train;

/**
 * All fare maths lives here, so changing a price rule means editing ONE class.
 *
 *   fare = train base fare x coach multiplier  -  age discount
 */
public class FareCalculator {

    // Coach multipliers (a Sleeper seat is the base)
    public static final double SLEEPER_MULTIPLIER = 1.0;
    public static final double AC3_MULTIPLIER = 1.5;
    public static final double AC2_MULTIPLIER = 2.0;

    // Age discounts (age limits are defined in Passenger)
    public static final double CHILD_DISCOUNT = 0.50;     // 50% off
    public static final double SENIOR_DISCOUNT = 0.40;    // 40% off

    // Returns the multiplier for a coach type
    public double getClassMultiplier(String coachType) {
        if (coachType.equals(Coach.TYPE_SLEEPER)) {
            return SLEEPER_MULTIPLIER;
        } else if (coachType.equals(Coach.TYPE_AC3)) {
            return AC3_MULTIPLIER;
        } else if (coachType.equals(Coach.TYPE_AC2)) {
            return AC2_MULTIPLIER;
        }
        throw new IllegalArgumentException("Unknown coach type: " + coachType);
    }

    // Returns the discount (0.0 = none, 0.5 = half price) for an age
    public double getDiscountRate(int age) {
        if (age <= Passenger.CHILD_MAX_AGE) {
            return CHILD_DISCOUNT;
        } else if (age >= Passenger.SENIOR_MIN_AGE) {
            return SENIOR_DISCOUNT;
        }
        return 0.0;
    }

    // Overload 1: full fare, no discount
    public double calculateFare(double baseFare, String coachType) {
        return baseFare * getClassMultiplier(coachType);
    }

    // Overload 2: fare after the age discount
    public double calculateFare(double baseFare, String coachType, int age) {
        double fullFare = calculateFare(baseFare, coachType);
        double discount = fullFare * getDiscountRate(age);
        return fullFare - discount;
    }

    // Overload 3: takes the objects themselves (used while booking)
    public double calculateFare(Train train, Coach coach, Passenger passenger) {
        return calculateFare(train.getBaseFare(), coach.getCoachType(), passenger.getAge());
    }

    // Total of all tickets that are NOT cancelled
    public double calculateTotalAmount(Ticket[] tickets) {
        double total = 0;
        for (int i = 0; i < tickets.length; i++) {
            if (!tickets[i].isCancelled()) {
                total = total + tickets[i].getFare();
            }
        }
        return total;
    }
}
