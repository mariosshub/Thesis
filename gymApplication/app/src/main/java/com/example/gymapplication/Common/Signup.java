package com.example.gymapplication.Common;

import static com.example.gymapplication.Api.ApiExceptions.EMAIL_EXISTS;
import static com.example.gymapplication.Api.ApiExceptions.USER_EXISTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Model.Role;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.SingUpRequest;
import com.example.gymapplication.Service.AuthService;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Signup extends AppCompatActivity {
    private EditText nameText, surnameText, emailText, phoneText, usernameText, passwordText;
    private ConstraintLayout parent;
    private AuthService authService;
    private LoadingData loadingData;
    private Resources res;
    private static final String DEFAULT_IMAGE_URL = "default_profile.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        res = getResources();

        initViews();

        loadingData = new LoadingData(this);
        loadingData.setProgressBarForConLayout(parent);
    }

    public void signup(View view){
        String name = nameText.getText().toString();
        String surname = surnameText.getText().toString();
        String email = emailText.getText().toString();
        String phone = phoneText.getText().toString();
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if(name.isEmpty()){
            nameText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(surname.isEmpty()){
            surnameText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(email.isEmpty()){
            emailText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(phone.isEmpty()){
            phoneText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(username.isEmpty()){
            usernameText.setError(res.getString(R.string.no_empty_fields));
            return;
        }
        else if(password.isEmpty()){
            passwordText.setError(res.getString(R.string.no_empty_fields));
            return;
        }

        loadingData.setEnableProgressBar();

        //signup request
        Call<Customer> call = authService.createUser(new SingUpRequest(name,surname,email,phone,username,password,DEFAULT_IMAGE_URL));
        call.enqueue(new Callback<Customer>(){
            @Override
            public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {
                if(!response.isSuccessful()){
                    if(response.errorBody() != null) {
                        try {
                            String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                            if(message.equals(USER_EXISTS))
                                Toast.makeText(Signup.this, getResources().getString(R.string.user_exists,username),Toast.LENGTH_SHORT).show();
                            else if(message.equals(EMAIL_EXISTS))
                                Toast.makeText(Signup.this, getResources().getString(R.string.email_exists,email),Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    loadingData.setDisableProgressBar();
                }
                if(response.body() != null){
                    Customer customerResponse = response.body();
                    Toast.makeText(Signup.this,getResources().getString(R.string.signup_success,username),Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Customer> call, @NonNull Throwable t) {
                Toast.makeText(Signup.this, t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    private void initViews(){
        parent = findViewById(R.id.sign_up);
        nameText = findViewById(R.id.editTextName);
        surnameText = findViewById(R.id.editTextSurname);
        emailText = findViewById(R.id.editTextSignupEmail);
        phoneText = findViewById(R.id.editTextPhone);
        usernameText = findViewById(R.id.editTextSignUpUsername);
        passwordText = findViewById(R.id.editTextSignUpPassword);
        authService = ApiClient.getAuthService();
    }
}