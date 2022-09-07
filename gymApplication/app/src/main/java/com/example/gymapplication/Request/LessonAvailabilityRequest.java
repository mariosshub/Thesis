package com.example.gymapplication.Request;

import com.example.gymapplication.Model.WorkoutLessons;

public class LessonAvailabilityRequest {
    private String date;
    private String startingHour;
    private String endHour;
    private WorkoutLessons workoutLessons;

    public LessonAvailabilityRequest(String date, String startingHour, String endHour, WorkoutLessons workoutLessons) {
        this.date = date;
        this.startingHour = startingHour;
        this.endHour = endHour;
        this.workoutLessons = workoutLessons;
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

    public WorkoutLessons getWorkoutLessons() {
        return workoutLessons;
    }

    public void setWorkoutLessons(WorkoutLessons workoutLessons) {
        this.workoutLessons = workoutLessons;
    }
}
