package com.marios.gymAppDemo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String date;
    private String startingHour;
    private String endHour;

    @ManyToOne
    @JoinColumn(name = "workoutLessons_id")
    @JsonBackReference
    private WorkoutLessons workoutLessons;
}
