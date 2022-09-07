package com.example.gymapplication.Response;

import com.example.gymapplication.Model.Status;


public class ReservationsResponse {
    private Long reservationId;
    private String dayOfWeek;
    private String date;
    private String hour;
    private Status status;
    private String workoutName;

    public ReservationsResponse(Long reservationId, String dayOfWeek, String date, String hour, Status status, String workoutName) {
        this.reservationId = reservationId;
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.workoutName = workoutName;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }
}
