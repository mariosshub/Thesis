package com.example.gymapplication.Service;

import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Request.LoginRequest;
import com.example.gymapplication.Request.RefreshTokenRequest;
import com.example.gymapplication.Request.SingUpRequest;
import com.example.gymapplication.Response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("/signup")
    Call<Customer> createUser(@Body SingUpRequest singUpRequest);

    @POST("/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("/refresh/token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    @POST("/logoutCustomer")
    Call<Void> logout(@Body RefreshTokenRequest refreshTokenRequest);
}
