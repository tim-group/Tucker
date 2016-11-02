package com.timgroup.tucker.info;

public final class Runbook {
    private final String locationURL;


    public Runbook(String locationURL) {
        this.locationURL = locationURL;
    }

    public String getLocation() {
        return locationURL;
    }
}
