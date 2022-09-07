package com.example.gymapplication.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Common.MenuActivity;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.RefreshTokenRequest;
import com.example.gymapplication.Response.AuthResponse;
import com.example.gymapplication.Service.AuthService;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JwtUtil {
    private Context mainActivityContext;
    private final AuthUtil authUtil;
    private final AuthService authService = ApiClient.getAuthService();
    private LoadingData loadingData;

    public JwtUtil(AuthUtil authUtil, Context mainActivityContext, LoadingData loadingData) {
        this.authUtil = authUtil;
        this.mainActivityContext = mainActivityContext;
        this.loadingData = loadingData;
    }

    public JwtUtil(AuthUtil authUtil)
    {
        this.authUtil = authUtil;
    }

    public void checkJwtAndRefresh(){
        //check if user hasn't logged out
        if (!authUtil.getUsername().isEmpty()){
            //refresh token
            refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
        }
    }

    public void refreshJwt(String username, String refreshToken){

        loadingData.setEnableProgressBar();
        Call<AuthResponse> call = authService.refreshToken(new RefreshTokenRequest(refreshToken,username));
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if(!response.isSuccessful()){
                    System.out.println(response.code());
                    if(response.errorBody() != null) {
                        try {
                            System.out.println(new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage());
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                        loadingData.setDisableProgressBar();
                    }
                }
                else{
                    if(response.body() != null){
                        AuthResponse auth = response.body();
                        authUtil.editAuth(auth.getAuthenticationToken(),auth.getRole(),auth.getUsername(),auth.getRefreshToken());

                        if(mainActivityContext != null){
                            Toast.makeText(mainActivityContext, mainActivityContext.getResources().getString(R.string.reconnecting_message), Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();

                            mainActivityContext.startActivity(new Intent(mainActivityContext,MenuActivity.class));
                            ((Activity) mainActivityContext).finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                System.out.println(t.getMessage());
                loadingData.setDisableProgressBar();
            }
        });
    }
}
