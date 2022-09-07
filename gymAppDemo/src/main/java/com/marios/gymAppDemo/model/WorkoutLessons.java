package com.marios.gymAppDemo.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutLessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    private double costPerDay;
    private double costPerWeek;
    private double costPerMonth;
    private double costPerYear;
    private Long maxPeeps;

    @OneToMany(targetEntity = LessonAvailability.class)
    @JsonManagedReference
    private List<LessonAvailability> lessonAvailabilities;

}
