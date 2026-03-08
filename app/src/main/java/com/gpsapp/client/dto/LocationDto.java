package com.gpsapp.client.dto;

public class LocationDto {
    private double latitude;
    private double longitude;
    String label;

    public LocationDto(double latitude, double longitude, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    public String getLabel() {
        return label;
    }
}
