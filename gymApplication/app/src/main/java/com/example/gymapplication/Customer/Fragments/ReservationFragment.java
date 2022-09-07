package com.example.gymapplication.Customer.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.AVAILABILITY_NOT_FOUND;
import static com.example.gymapplication.Api.ApiExceptions.BOOKED_LESSONS;
import static com.example.gymapplication.Api.ApiExceptions.CUSTOMER_NOT_SUBSCRIBED;
import static com.example.gymapplication.Api.ApiExceptions.RESERVATION_AVAILABLE_HOURS;
import static com.example.gymapplication.Api.ApiExceptions.SUB_LESSON_EXPIRED;
import static com.example.gymapplication.Api.ApiExceptions.SUB_NOT_FOUND;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Common.MainActivity;
import com.example.gymapplication.Model.DaysOfWeekConstant;
import com.example.gymapplication.Request.CountForReservationsRequest;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.Reservations;
import com.example.gymapplication.R;
import com.example.gymapplication.Broadcast.ReminderBroadcast;
import com.example.gymapplication.Request.MakeReservationRequest;
import com.example.gymapplication.Response.AvailabilityForResResponse;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationFragment extends Fragment implements View.OnClickListener {

    private TextView workoutName, workoutDay, workoutAvailability, startEndTime;
    private ProgressBar availabilityBar;
    private Spinner timeSpinner;
    private Button confirmBtn;
    private RelativeLayout parent;
    private CustomerService customerService;
    private AvailabilityForResResponse currentAvailability;

    private String selectedTime;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_fragment,container,false);
        res = requireActivity().getResources();
        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        customerService = ApiClient.getCustomerService();
        createNotificationChannel();

        Bundle bundle = this.getArguments();
        if(bundle != null){
            currentAvailability = (AvailabilityForResResponse) bundle.getSerializable("currentAvailability");
        }

        if(currentAvailability != null){
            workoutName.setText(currentAvailability.getWorkoutName());
            // localize date text
            switch (currentAvailability.getDate()){
                case DaysOfWeekConstant.MONDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.monday));
                    break;
                case DaysOfWeekConstant.TUESDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.tuesday));
                    break;
                case DaysOfWeekConstant.WEDNESDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.wednesday));
                    break;
                case DaysOfWeekConstant.THURSDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.thursday));
                    break;
                case DaysOfWeekConstant.FRIDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.friday));
                    break;
                case DaysOfWeekConstant.SATURDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.saturday));
                    break;
                case DaysOfWeekConstant.SUNDAY:
                    workoutDay.setText(requireActivity().getResources().getString(R.string.sunday));
                    break;
            }
            initResTime();
        }

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTime = (String) parent.getItemAtPosition(position);

                int endTimeWorkout = Integer.parseInt(selectedTime.split(":")[0].replaceFirst("^0*",""));
                endTimeWorkout +=1;

                // format time string to match specific pattern (ex. 01:00)
                String formattedTime ;
                if(endTimeWorkout <= 9){
                    formattedTime = String.format("%02d:00", endTimeWorkout);
                }
                else{
                    formattedTime = String.format("%d:00", endTimeWorkout);
                }

                startEndTime.setText(res.getString(R.string.reservations_start_end_time,selectedTime,formattedTime));
                setAvailabilityBar(selectedTime);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        confirmBtn.setOnClickListener(this);
        return view;
    }

    private void setAvailabilityBar(String hour){
        AuthUtil authUtil = new AuthUtil(requireActivity());
        loadingData.setEnableProgressBar();

        Call<Long> call = customerService.countReservations(authUtil.getToken(),
                new CountForReservationsRequest(currentAvailability.getAvailabilityId(),currentAvailability.getDate(),hour));
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(requireActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                Toast.makeText(requireActivity(),new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                else{
                    if(response.body() != null){
                        Long count = response.body();
                        Long maxPeopleForLesson = currentAvailability.getMaxPeeps();
                        availabilityBar.setMax(maxPeopleForLesson.intValue());
                        if(count.equals(maxPeopleForLesson)){
                            workoutAvailability.setText(res.getString(R.string.lesson_full_message));
                            availabilityBar.setProgress(maxPeopleForLesson.intValue());
                            Toast.makeText(requireActivity(), res.getString(R.string.lesson_full_message), Toast.LENGTH_SHORT).show();
                            confirmBtn.setVisibility(View.GONE);

                        }
                        else if(count + 1 <= maxPeopleForLesson){
                            workoutAvailability.setText(res.getString(R.string.lesson_available_for,String.valueOf(maxPeopleForLesson-count),String.valueOf(maxPeopleForLesson)));
                            availabilityBar.setProgress(count.intValue());
                        }
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    // show the time between the start of the workout lesson and the end.
    private void initResTime(){
        int currentHour = LocalDateTime.now().getHour();
        int startTimeWorkout = Integer.parseInt(currentAvailability.getStartingHour().split(":")[0].replaceFirst("^0*",""));
        int endTimeWorkout = Integer.parseInt(currentAvailability.getEndHour().split(":")[0].replaceFirst("^0*",""));

        if(currentHour >= startTimeWorkout && currentHour < endTimeWorkout){
            startTimeWorkout = currentHour + 1;
        }

        String[] startTimeRes = new String[endTimeWorkout - startTimeWorkout];

        int j = 0;
        for (int i = startTimeWorkout; i < endTimeWorkout; i++) {
            if(i <= 9){
                startTimeRes[j] = String.format("%02d:00", i);
            }
            else{
                startTimeRes[j] = String.format("%d:00", i);
            }
            j++;
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, startTimeRes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(arrayAdapter);

    }

    private void initViews(View view){
        parent = view.findViewById(R.id.reservation_fragment);
        workoutName = view.findViewById(R.id.reservationsWorkout);
        workoutDay = view.findViewById(R.id.reservationsDay);
        workoutAvailability = view.findViewById(R.id.reservationsAvailability);
        availabilityBar = view.findViewById(R.id.reservationsWorkoutAvailabilityBar);
        startEndTime = view.findViewById(R.id.reservationsStartEndTime);
        timeSpinner = view.findViewById(R.id.reservationsTimeSpinner);
        confirmBtn = view.findViewById(R.id.reservationsConfirmBtn);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.reservationsConfirmBtn){
            AuthUtil authUtil = new AuthUtil(requireActivity());

            MakeReservationRequest reservationRequest = new MakeReservationRequest(authUtil.getUsername(),currentAvailability.getAvailabilityId(),selectedTime);
            loadingData.setEnableProgressBar();

            Call<Boolean> call = customerService.makeReservation(authUtil.getToken(),reservationRequest);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    if(!response.isSuccessful()){
                        if(response.code() == 403){
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                            Toast.makeText(requireActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(response.errorBody() != null) {
                                try {
                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                    if(message.equals(SUB_NOT_FOUND))
                                        Toast.makeText(requireActivity(),res.getString(R.string.sub_not_found),Toast.LENGTH_SHORT).show();
                                    else if(message.equals(AVAILABILITY_NOT_FOUND))
                                        Toast.makeText(requireActivity(),res.getString(R.string.availability_not_found),Toast.LENGTH_SHORT).show();
                                    else if(message.equals(BOOKED_LESSONS))
                                        Toast.makeText(requireActivity(),res.getString(R.string.booked_lessons),Toast.LENGTH_SHORT).show();
                                    else if(message.equals(RESERVATION_AVAILABLE_HOURS))
                                        Toast.makeText(requireActivity(),res.getString(R.string.res_available_hour),Toast.LENGTH_SHORT).show();
                                    else if(message.equals(SUB_LESSON_EXPIRED))
                                        Toast.makeText(requireActivity(),res.getString(R.string.sub_lesson_exp),Toast.LENGTH_SHORT).show();
                                    else if(message.equals(CUSTOMER_NOT_SUBSCRIBED))
                                        Toast.makeText(requireActivity(),res.getString(R.string.customer_not_sub),Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        loadingData.setDisableProgressBar();
                    }
                    else{
                        if(response.body() != null && response.body()){
                            Toast.makeText(requireActivity(),res.getString(R.string.reservation_success_message),Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();

                            setAlarmAndNotification();
                            requireActivity().getSupportFragmentManager().popBackStack();
                            // show reservations to customer
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ShowAllReservationsFragment()).addToBackStack(null).commit();

                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }
    }

    private void setAlarmAndNotification(){
        Intent intent = new Intent(requireActivity(), ReminderBroadcast.class);
        intent.putExtra("woName", currentAvailability.getWorkoutName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireActivity(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        long currentTime = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int lessonTime = Integer.parseInt(selectedTime.split(":")[0].replaceFirst("^0*",""));
        c.set(Calendar.HOUR_OF_DAY,lessonTime);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        long timeToAdd = c.getTimeInMillis() - currentTime;
        if(timeToAdd > 0){
            String untilStart = res.getString(R.string.lesson_starts_message,timeToAdd/(1000*60*60), (timeToAdd%(1000*60*60))/(1000*60), ((timeToAdd%(1000*60*60))%(1000*60))/1000);
            Toast.makeText(requireActivity(),untilStart,Toast.LENGTH_SHORT).show();
            alarmManager.set(AlarmManager.RTC_WAKEUP,currentTime + timeToAdd,pendingIntent);
        }
        else{
            throw new IllegalArgumentException("Time can't have negative values");
        }

    }

    private void createNotificationChannel(){
        CharSequence name = "GymAppReminderChannel";
        String description = "Channel for Customer Reminder";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("notifyCustomer",name,importance);
        channel.setDescription(description);

        NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
