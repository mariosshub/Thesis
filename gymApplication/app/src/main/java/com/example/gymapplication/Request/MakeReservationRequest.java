package com.example.gymapplication.Request;

public class MakeReservationRequest {
    private String customersUsername;
    private Long availabilityId;
    private String hourOfReservation;

    public MakeReservationRequest(String customersUsername, Long availabilityId, String hourOfReservation) {
        this.customersUsername = customersUsername;
        this.availabilityId = availabilityId;
        this.hourOfReservation = hourOfReservation;
    }

    public String getCustomersUsername() {
        return customersUsername;
    }

    public void setCustomersUsername(String customersUsername) {
        this.customersUsername = customersUsername;
    }

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public String getHourOfReservation() {
        return hourOfReservation;
    }

    public void setHourOfReservation(String hourOfReservation) {
        this.hourOfReservation = hourOfReservation;
    }
}
