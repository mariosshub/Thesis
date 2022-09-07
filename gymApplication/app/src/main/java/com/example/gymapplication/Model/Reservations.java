package com.example.gymapplication.Model;


public class Reservations {
    private Long id;
    private String date;
    private String hour;
    private Status status;
    private Subscription subscription;
    private LessonAvailability lessonAvailability;

    public Reservations(Long id, String date, String hour, Status status, Subscription subscription, LessonAvailability lessonAvailability) {
        this.id = id;
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.subscription = subscription;
        this.lessonAvailability = lessonAvailability;
    }

    public Reservations(String date, String hour, Status status, Subscription subscription, LessonAvailability lessonAvailability) {
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.subscription = subscription;
        this.lessonAvailability = lessonAvailability;
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

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public LessonAvailability getLessonAvailability() {
        return lessonAvailability;
    }

    public void setLessonAvailability(LessonAvailability lessonAvailability) {
        this.lessonAvailability = lessonAvailability;
    }
}
