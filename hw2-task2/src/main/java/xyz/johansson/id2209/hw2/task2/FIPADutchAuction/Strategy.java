package xyz.johansson.id2209.hw2.task2.FIPADutchAuction;

import java.util.Random;

public enum Strategy {
    LOW, MEDIUM, HIGH;

    public static Strategy getRandom() {
        return values()[new Random().nextInt(values().length)];
    }

    @Override
    public String toString() {
        switch (this) {
            case LOW:
                return "low";
            case MEDIUM:
                return "medium";
            default:
                return "high";
        }
    }
}