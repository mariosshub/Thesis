package com.example.gymapplication.Model;

public class SubscriptionWorkoutLessons {
    private Long id;
    private String cost;
    private double lessonCost;
    private int amount;
    private String createdAt;
    private String expiresAt;
    private boolean expired;
    private WorkoutLessons workoutLesson;

    public SubscriptionWorkoutLessons(Long id, String cost, double lessonCost, int amount, String createdAt, String expiresAt, boolean expired, WorkoutLessons workoutLesson) {
        this.id = id;
        this.cost = cost;
        this.lessonCost = lessonCost;
        this.amount = amount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
        this.workoutLesson = workoutLesson;
    }

    public SubscriptionWorkoutLessons(String cost, double lessonCost, int amount, String createdAt, String expiresAt, boolean expired, WorkoutLessons workoutLesson) {
        this.cost = cost;
        this.lessonCost = lessonCost;
        this.amount = amount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
        this.workoutLesson = workoutLesson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public double getLessonCost() {
        return lessonCost;
    }

    public void setLessonCost(double lessonCost) {
        this.lessonCost = lessonCost;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public WorkoutLessons getWorkoutLesson() {
        return workoutLesson;
    }

    public void setWorkoutLesson(WorkoutLessons workoutLesson) {
        this.workoutLesson = workoutLesson;
    }
}
