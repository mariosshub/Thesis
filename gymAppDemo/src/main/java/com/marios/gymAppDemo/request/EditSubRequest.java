package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditSubRequest {
    private Long subId;
    private Long subWorkoutLessonId;
    private String cost;
    private int amount;
}
