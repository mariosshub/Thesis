package com.example.gymapplication.Response;

import java.io.Serializable;


public class AvailabilityForResResponse implements Serializable {
    private Long availabilityId;
    private Long workoutId;
    private String workoutName;
    private String date;
    private String startingHour;
    private String endHour;
    private Long maxPeeps;

    public AvailabilityForResResponse(Long availabilityId, Long workoutId, String workoutName, String date, String startingHour, String endHour, Long maxPeeps) {
        this.availabilityId = availabilityId;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.date = date;
        this.startingHour = startingHour;
        this.endHour = endHour;
        this.maxPeeps = maxPeeps;
    }

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(Long workoutId) {
        this.workoutId = workoutId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartingHour() {
        return startingHour;
    }

    public void setStartingHour(String startingHour) {
        this.startingHour = startingHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public Long getMaxPeeps() {
        return maxPeeps;
    }

    public void setMaxPeeps(Long maxPeeps) {
        this.maxPeeps = maxPeeps;
    }
}
