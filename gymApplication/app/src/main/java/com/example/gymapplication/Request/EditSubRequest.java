package com.example.gymapplication.Request;


public class EditSubRequest {
    private Long subId;
    private Long subWorkoutLessonId;
    private String cost;
    private int amount;

    public EditSubRequest(Long subId, Long subWorkoutLessonId, String cost, int amount) {
        this.subId = subId;
        this.subWorkoutLessonId = subWorkoutLessonId;
        this.cost = cost;
        this.amount = amount;
    }

    public Long getSubId() {
        return subId;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
    }

    public Long getSubWorkoutLessonId() {
        return subWorkoutLessonId;
    }

    public void setSubWorkoutLessonId(Long subWorkoutLessonId) {
        this.subWorkoutLessonId = subWorkoutLessonId;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
