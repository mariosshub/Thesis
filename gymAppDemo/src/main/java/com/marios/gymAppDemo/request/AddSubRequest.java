package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSubRequest {
    private String customersEmail;
    private Long workoutId;
    private String cost;
    private int amount;
}
