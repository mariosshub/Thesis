package com.marios.gymAppDemo.request;

import com.marios.gymAppDemo.model.WorkoutLessons;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonAvailabilityRequest {
    private String date;
    private String startingHour;
    private String endHour;
    private WorkoutLessons workoutLessons;
}
