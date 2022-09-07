package com.marios.gymAppDemo.response;

import com.marios.gymAppDemo.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationsResponse {
    private Long reservationId;
    private String dayOfWeek;
    private String date;
    private String hour;
    private Status status;
    private String workoutName;

}
