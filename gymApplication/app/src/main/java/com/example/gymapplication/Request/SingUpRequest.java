package com.example.gymapplication.Request;


public class SingUpRequest {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String username;
    private String password;
    private String imgUrl;

    public SingUpRequest(String name, String surname, String email, String phone, String username, String password, String imgUrl) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.imgUrl = imgUrl;
    }
}
