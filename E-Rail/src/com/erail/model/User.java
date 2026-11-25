package com.erail.model;

/**
 * Abstract class: holds the details common to any person using the system.
 * We never create a plain User; we create a subclass such as Passenger.
 * Fields are private (encapsulation) and are reached through getters/setters.
 */
public abstract class User {

    private String name;
    private int age;
    private String gender;

    public User(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    // Abstract method: every subclass MUST decide how its details are shown
    public abstract String getDetails();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
