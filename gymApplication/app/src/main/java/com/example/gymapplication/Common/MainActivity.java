package com.example.gymapplication.Common;

import static com.example.gymapplication.Api.ApiExceptions.BAD_CREDS;
import static com.example.gymapplication.Api.ApiExceptions.USER_NOT_FOUND;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.LoginRequest;
import com.example.gymapplication.Response.AuthResponse;
import com.example.gymapplication.Service.AuthService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AuthService authService;
    private EditText usernameText, passwordText;
    private LoadingData loadingData;
    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();

        setContentView(R.layout.activity_main);
        ConstraintLayout layout = findViewById(R.id.activity_main);
        usernameText = findViewById(R.id.editTextUsername);
        passwordText = findViewById(R.id.editTextPassword);

        loadingData = new LoadingData(this);
        loadingData.setProgressBarForConLayout(layout);

        authService = ApiClient.getAuthService();

        //check if user needs to log in
        AuthUtil authUtil = new AuthUtil(this);
        JwtUtil jwtUtil = new JwtUtil(authUtil,this,loadingData);
        jwtUtil.checkJwtAndRefresh();

    }


    public void gotoSignup(View view){
        startActivity(new Intent(this, Signup.class));
    }


    public void login(View view) {
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if(username.isEmpty()){
            usernameText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(password.isEmpty()){
            passwordText.setError(res.getString(R.string.no_empty_fields));
            return;
        }

        loadingData.setEnableProgressBar();

        // login user with username and password
        Call<AuthResponse> call = authService.login(new LoginRequest(username,password));
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if(!response.isSuccessful()){
                    if(response.errorBody() != null) {
                        try {
                            String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                            if(message.equals(USER_NOT_FOUND))
                                Toast.makeText(MainActivity.this,res.getString(R.string.user_not_found),Toast.LENGTH_SHORT).show();
                            else if(message.equals(BAD_CREDS))
                                Toast.makeText(MainActivity.this,res.getString(R.string.bad_creds),Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    loadingData.setDisableProgressBar();
                    return;
                }
                if(response.body() != null){
                    AuthResponse auth = response.body();

                    //save authentication details of user
                    AuthUtil authUtil = new AuthUtil(MainActivity.this);
                    authUtil.editAuth(auth.getAuthenticationToken(),auth.getRole(),auth.getUsername(),auth.getRefreshToken());

                    Toast.makeText(MainActivity.this, res.getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MainActivity.this, MenuActivity.class));
                    finish();
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });

    }
}