package com.example.gymapplication.Request;


public class AddSubRequest {
    private String customersEmail;
    private Long workoutId;
    private String cost;
    private int amount;

    public AddSubRequest(String customersEmail, Long workoutId, String cost, int amount) {
        this.customersEmail = customersEmail;
        this.workoutId = workoutId;
        this.cost = cost;
        this.amount = amount;
    }

    public String getCustomersEmail() {
        return customersEmail;
    }

    public void setCustomersEmail(String customersEmail) {
        this.customersEmail = customersEmail;
    }

    public Long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(Long workoutId) {
        this.workoutId = workoutId;
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
