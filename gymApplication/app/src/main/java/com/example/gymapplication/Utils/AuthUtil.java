package com.example.gymapplication.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.gymapplication.Model.Role;

public class AuthUtil {
    private final SharedPreferences sharedPreferences;

    public AuthUtil(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
    }

    public String getToken(){
        return "Bearer " +sharedPreferences.getString("token","");
    }

    public String getRole() {
        return sharedPreferences.getString("role","");
    }

    public String getUsername() {
        return sharedPreferences.getString("username","");
    }

    public String getRefreshToken(){return sharedPreferences.getString("refreshToken","");}

    public void editAuth(String authToken, Role role, String username, String refreshToken){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token",authToken);
        editor.putString("role",role.name());
        editor.putString("username",username);
        editor.putString("refreshToken",refreshToken);
        editor.commit();
    }

}
