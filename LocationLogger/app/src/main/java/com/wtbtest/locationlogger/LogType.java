package com.wtbtest.locationlogger;

public enum LogType {
    FUSEDPOSITION("fused/position",0),
    FUSEDACCURACY("fused/accuracy",1),
    RAWGNSS("raw-gnss",2),
    COMPUTEDGNSS("computed-gnss",3);

    private String stringValue;
    private int intValue;

    private LogType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
