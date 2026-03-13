package com.example.expensestracker.model.enums;

public enum Gender {
    male("male"),
    female("female"),
    other("other");
    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
