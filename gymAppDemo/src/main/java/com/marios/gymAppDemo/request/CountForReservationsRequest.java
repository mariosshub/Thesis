package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountForReservationsRequest {
    Long availabilityId;
    String dayOfWeek;
    String hour;
}
