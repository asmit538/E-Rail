package com.erail.model;

/**
 * A person travelling on a train. Inherits name, age and gender from User and
 * adds passenger-specific data: a unique passenger ID and an age category.
 */
public class Passenger extends User {

    // Age limits used for the age category AND for fare discounts (see FareCalculator)
    public static final int CHILD_MAX_AGE = 12;
    public static final int SENIOR_MIN_AGE = 60;

    // static: shared by ALL passengers, so every new passenger gets the next number
    private static int nextPassengerId = 1;

    private int passengerId;

    public Passenger(String name, int age, String gender) {
        super(name, age, gender);              // pass common data up to User
        this.passengerId = nextPassengerId;
        nextPassengerId++;
    }

    // Decides the category from the age
    public String getAgeCategory() {
        if (getAge() <= CHILD_MAX_AGE) {
            return "Child";
        } else if (getAge() >= SENIOR_MIN_AGE) {
            return "Senior";
        } else {
            return "Adult";
        }
    }

    // Method overriding: gives the abstract User.getDetails() a real body
    @Override
    public String getDetails() {
        return String.format("P%03d | %s | Age %d | %s | %s",
                passengerId, getName(), getAge(), getGender(), getAgeCategory());
    }

    public int getPassengerId() {
        return passengerId;
    }
}
