package com.example.expensestracker.model.enums;

public enum RepeatFrequency {
    daily("daily"),
    weekly("weekly"),
    monthly("monthly"),
    yearly("yearly");
    private final String repeatFrequency;
    RepeatFrequency(String repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    public String getRepeatFrequency() {
        return repeatFrequency;
    }
    public static RepeatFrequency fromString(String repeatFrequency) {
        for (RepeatFrequency repeat : RepeatFrequency.values()) {
            if (repeat.getRepeatFrequency().equalsIgnoreCase(repeatFrequency)) {
                return repeat;
            }
        }
        throw new IllegalArgumentException("Invalid category type: " + repeatFrequency);
    }
}
