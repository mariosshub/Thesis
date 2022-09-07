package com.example.gymapplication.Model;

import java.util.List;


public class Subscription {
    private long id;
    private SubscriptionCustomerField customer;
    private double cost;
    private List<SubscriptionWorkoutLessons> subscriptionWorkoutLessons;

    public Subscription(long id, SubscriptionCustomerField customer, double cost, List<SubscriptionWorkoutLessons> subscriptionWorkoutLessons) {
        this.id = id;
        this.customer = customer;
        this.cost = cost;
        this.subscriptionWorkoutLessons = subscriptionWorkoutLessons;
    }

    public Subscription(SubscriptionCustomerField customer, double cost, List<SubscriptionWorkoutLessons> subscriptionWorkoutLessons) {
        this.customer = customer;
        this.cost = cost;
        this.subscriptionWorkoutLessons = subscriptionWorkoutLessons;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SubscriptionCustomerField getCustomer() {
        return customer;
    }

    public void setCustomer(SubscriptionCustomerField customer) {
        this.customer = customer;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<SubscriptionWorkoutLessons> getSubscriptionWorkoutLessons() {
        return subscriptionWorkoutLessons;
    }

    public void setSubscriptionWorkoutLessons(List<SubscriptionWorkoutLessons> subscriptionWorkoutLessons) {
        this.subscriptionWorkoutLessons = subscriptionWorkoutLessons;
    }
}
