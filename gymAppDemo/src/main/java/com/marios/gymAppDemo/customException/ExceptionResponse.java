package com.marios.gymAppDemo.customException;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private Date timestamp;
    private String status;
    private String message;
    private String path;

}
