package com.marios.gymAppDemo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String username;
    private String password;
    private String imgUrl;
}
