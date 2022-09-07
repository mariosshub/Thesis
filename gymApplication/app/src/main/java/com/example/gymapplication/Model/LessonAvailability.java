package com.example.gymapplication.Model;


public class LessonAvailability {
    private Long id;
    private String date;
    private String startingHour;
    private String endHour;
    private WorkoutLessons workoutLessons;

    public LessonAvailability(Long id, String date, String startingHour, String endHour, WorkoutLessons workoutLessons) {
        this.id = id;
        this.date = date;
        this.startingHour = startingHour;
        this.endHour = endHour;
        this.workoutLessons = workoutLessons;
    }

    public LessonAvailability(String date, String startingHour, String endHour, WorkoutLessons workoutLessons) {
        this.date = date;
        this.startingHour = startingHour;
        this.endHour = endHour;
        this.workoutLessons = workoutLessons;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
