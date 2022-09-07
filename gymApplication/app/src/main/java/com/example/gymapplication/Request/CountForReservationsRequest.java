package com.example.gymapplication.Request;

public class CountForReservationsRequest {
    Long availabilityId;
    String dayOfWeek;
    String hour;

    public CountForReservationsRequest(){};
    public CountForReservationsRequest(Long availabilityId, String dayOfWeek, String hour) {
        this.availabilityId = availabilityId;
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
    }
}
