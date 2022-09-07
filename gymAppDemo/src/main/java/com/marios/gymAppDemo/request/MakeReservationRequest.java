package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakeReservationRequest {
    private String customersUsername;
    private Long availabilityId;
    private String hourOfReservation;

}
