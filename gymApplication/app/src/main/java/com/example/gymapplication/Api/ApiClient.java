package com.example.gymapplication.Api;

import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Service.AuthService;
import com.example.gymapplication.Service.CustomerService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit getRetrofit(){

        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://192.168.1.5:8080/")
                .build();
    }

    public static AuthService getAuthService(){
        return getRetrofit().create(AuthService.class);
    }

    public static CustomerService getCustomerService(){
        return getRetrofit().create(CustomerService.class);
    }

    public static AdminService getAdminService(){
        return getRetrofit().create(AdminService.class);
    }
}
