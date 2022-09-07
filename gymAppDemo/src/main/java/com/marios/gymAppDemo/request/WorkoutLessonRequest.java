package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutLessonRequest {
    private Long id;
    private String name;
    private String description;
    private double costPerDay;
    private double costPerWeek;
    private double costPerMonth;
    private double costPerYear;
    private Long maxPeeps;
}
