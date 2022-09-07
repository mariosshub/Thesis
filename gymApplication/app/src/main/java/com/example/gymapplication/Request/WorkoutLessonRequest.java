package com.example.gymapplication.Request;

public class WorkoutLessonRequest {
    private Long id;
    private String name;
    private String description;
    private double costPerDay;
    private double costPerWeek;
    private double costPerMonth;
    private double costPerYear;
    private Long maxPeeps;

    public WorkoutLessonRequest(Long id, String name, String description, double costPerDay, double costPerWeek, double costPerMonth, double costPerYear, Long maxPeeps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.costPerDay = costPerDay;
        this.costPerWeek = costPerWeek;
        this.costPerMonth = costPerMonth;
        this.costPerYear = costPerYear;
        this.maxPeeps = maxPeeps;
    }

    public WorkoutLessonRequest(String name, String description, double costPerDay, double costPerWeek, double costPerMonth, double costPerYear, Long maxPeeps) {
        this.name = name;
        this.description = description;
        this.costPerDay = costPerDay;
        this.costPerWeek = costPerWeek;
        this.costPerMonth = costPerMonth;
        this.costPerYear = costPerYear;
        this.maxPeeps = maxPeeps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCostPerDay() {
        return costPerDay;
    }

    public void setCostPerDay(double costPerDay) {
        this.costPerDay = costPerDay;
    }

    public double getCostPerWeek() {
        return costPerWeek;
    }

    public void setCostPerWeek(double costPerWeek) {
        this.costPerWeek = costPerWeek;
    }

    public double getCostPerMonth() {
        return costPerMonth;
    }

    public void setCostPerMonth(double costPerMonth) {
        this.costPerMonth = costPerMonth;
    }

    public double getCostPerYear() {
        return costPerYear;
    }

    public void setCostPerYear(double costPerYear) {
        this.costPerYear = costPerYear;
    }

    public Long getMaxPeeps() {
        return maxPeeps;
    }

    public void setMaxPeeps(Long maxPeeps) {
        this.maxPeeps = maxPeeps;
    }
}
