package com.marios.gymAppDemo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class SubscriptionWorkoutLessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cost;
    private double lessonCost;
    private int amount;
    private String createdAt;
    private String expiresAt;
    private boolean expired;

    @OneToOne
    @JoinColumn(name = "workout_lessons_id")
    private WorkoutLessons workoutLesson;
}
