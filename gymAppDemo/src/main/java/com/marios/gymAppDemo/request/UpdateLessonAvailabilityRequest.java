package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLessonAvailabilityRequest {
    private Long id;
    private String date;
    private String startingHour;
    private String endHour;
    private Long lessonId;
    private Long workoutId;
}
