package com.example.gymapplication.Common;

import static com.example.gymapplication.Api.ApiExceptions.SOMETHING_GONE_WRONG;
import static com.example.gymapplication.Api.ApiExceptions.SUB_NOT_FOUND;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymapplication.Admin.Fragments.AdminWorkoutLessonsFragment;
import com.example.gymapplication.Admin.Fragments.StatisticsFragment;
import com.example.gymapplication.Admin.Fragments.SubscriptionsFragment;
import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Customer.Fragments.CustomerSubscriptionFragment;
import com.example.gymapplication.Customer.Fragments.ShowAllReservationsFragment;
import com.example.gymapplication.Customer.Fragments.WorkoutCalendarFragment;
import com.example.gymapplication.Model.Role;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.RefreshTokenRequest;
import com.example.gymapplication.Service.AuthService;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private AuthUtil authUtil;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        res = this.getResources();

        authUtil = new AuthUtil(this);
        System.out.println(authUtil.getToken());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        TextView username = navigationView.getHeaderView(0).findViewById(R.id.username_header);
        username.setText(authUtil.getUsername());

        if (authUtil.getRole().equals(Role.ADMIN.name())) {
            this.setTitle(res.getString(R.string.app_name_admin));
            navigationView.inflateMenu(R.menu.admin_menu);
            navigationView.setCheckedItem(R.id.nav_admin_profile);
        } else {
            navigationView.inflateMenu(R.menu.customer_menu);
            navigationView.setCheckedItem(R.id.nav_customer_profile);

            //check if customers sub is expired
            checkCustomersSub();
        }

    }

    // when edit profile resume and show the updated profile fields
    @Override
    protected void onResume() {
        super.onResume();

        // check if resumed because of image choosing at profile fragment
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean("editPhoto",false)){
            editor.putBoolean("editPhoto",false);
            editor.apply();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
        }

    }

    // the navigation bar
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (authUtil.getRole().equals(Role.ADMIN.name())) {
            if(item.getItemId() == R.id.nav_admin_profile){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_workout_programs){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminWorkoutLessonsFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_subscription){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubscriptionsFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_stats){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatisticsFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_admin_logout){
                checkIfOkToLogout();
            }

        } else {
            if(item.getItemId() == R.id.nav_customer_profile){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_calendar) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WorkoutCalendarFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_reservations) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ShowAllReservationsFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_customer_subscription) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CustomerSubscriptionFragment()).addToBackStack(null).commit();
            }
            else if(item.getItemId() == R.id.nav_customer_logout) {
                logout();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            int count = getSupportFragmentManager().getBackStackEntryCount();

            System.out.println(count);
            //check if the back stack is at size 0 which means there are no other fragments open
            if (count == 0) {
                new AlertDialog.Builder(this)
                        .setMessage(res.getString(R.string.exit_dialog_message))
                        .setCancelable(false)
                        .setPositiveButton(res.getString(R.string.yes_dialog_message) , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MenuActivity.super.onBackPressed();
                            }
                        })
                        .setNegativeButton(res.getString(R.string.no_dialog_message),null)
                        .show();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    // if the user logged in is a customer, check subscription if is expired
    private void checkCustomersSub() {
        CustomerService customerService = ApiClient.getCustomerService();

        Call<Boolean> call = customerService.checkSubExpiration(authUtil.getToken());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(MenuActivity.this, res.getString(R.string.auth_renewed) , Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                if(message.equals(SUB_NOT_FOUND))
                                    Toast.makeText(MenuActivity.this,res.getString(R.string.sub_not_found),Toast.LENGTH_SHORT).show();
                                else if(message.equals(SOMETHING_GONE_WRONG))
                                    Toast.makeText(MenuActivity.this,res.getString(R.string.smth_wrong_message),Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                else{
                    if (response.body() != null){
                        boolean hasExpiredLessons = response.body();
                        if(hasExpiredLessons)
                            Toast.makeText(MenuActivity.this, res.getString(R.string.expired_lessons_message) , Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(MenuActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //if the user logged in is an admin, check if any workout lesson doesn't have an availability
    private void checkIfOkToLogout(){
        AdminService adminService = ApiClient.getAdminService();
        Call<Boolean> call = adminService.checkLessonsAvailabilitiesIfEmpty(authUtil.getToken());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(MenuActivity.this, res.getString(R.string.auth_renewed) , Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                Toast.makeText(MenuActivity.this,new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                else{
                    if(response.body() != null){
                        boolean hasLessonsWithoutAvailabilities = response.body();
                        if(hasLessonsWithoutAvailabilities)
                            Toast.makeText(MenuActivity.this, res.getString(R.string.no_availabilities_inserted_message) , Toast.LENGTH_LONG).show();
                        else
                            logout();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(MenuActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout(){
        new AlertDialog.Builder(this)
                .setMessage(res.getString(R.string.logout_dialog_message))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes_dialog_message) , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AuthService authService = ApiClient.getAuthService();
                        Call<Void> call = authService.logout(new RefreshTokenRequest(authUtil.getRefreshToken(),authUtil.getUsername()));
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if(!response.isSuccessful()){
                                    Toast.makeText(MenuActivity.this,res.getString(R.string.smth_wrong_message), Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    authUtil.editAuth("",Role.UNKNOWN,"","");
                                    startActivity(new Intent(MenuActivity.this, MainActivity.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                Toast.makeText(MenuActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(res.getString(R.string.no_dialog_message), null)
                .show();
    }
}
