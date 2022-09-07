package com.marios.gymAppDemo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityForResResponse {
    private Long availabilityId;
    private Long workoutId;
    private String workoutName;
    private String date;
    private String startingHour;
    private String endHour;
    private Long maxPeeps;
}
