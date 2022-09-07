package com.marios.gymAppDemo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Reservations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dayOfWeek;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate date;
    private String hour;
    @Enumerated(EnumType.STRING)
    private Status status;
    @OneToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
    @OneToOne
    @JoinColumn(name = "lessonAvailability_id")
    private LessonAvailability lessonAvailability;


    public Reservations(String dayOfWeek,LocalDate date, String hour, Status status, Subscription subscription, LessonAvailability lessonAvailability) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.subscription = subscription;
        this.lessonAvailability = lessonAvailability;
    }

}
