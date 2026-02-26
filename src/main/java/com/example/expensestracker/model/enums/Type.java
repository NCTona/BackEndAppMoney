package com.example.expensestracker.model.enums;

public enum Type {
    income ("income"),
    expense ("expense");
    private final String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    public static Type fromString(String type) {
        for (Type typee : Type.values()) {
            if (typee.getType().equalsIgnoreCase(type)) {
                return typee;
            }
        }
        throw new IllegalArgumentException("Invalid category type: " + type);
    }
}
