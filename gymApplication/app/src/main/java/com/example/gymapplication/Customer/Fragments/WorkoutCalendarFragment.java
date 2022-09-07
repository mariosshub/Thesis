package com.example.gymapplication.Customer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Customer.Adapters.WorkoutsForReservationAdapter;
import com.example.gymapplication.R;
import com.example.gymapplication.Response.AvailabilityForResResponse;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutCalendarFragment extends Fragment {
    private MaterialCalendarView materialCalendarView;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workout_calendar_fragment,container,false);

        materialCalendarView = view.findViewById(R.id.calendarView);
        materialCalendarView.setSelectedDate(new Date());
        materialCalendarView.setCurrentDate(CalendarDay.today());

        RelativeLayout parent = view.findViewById(R.id.workout_calendar_fragment);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        String currentDayOfWeek = new SimpleDateFormat("EEEE", Locale.UK).format(materialCalendarView.getSelectedDate().getDate());

        CustomerService customerService = ApiClient.getCustomerService();
        AuthUtil authUtil = new AuthUtil(requireActivity());

        RecyclerView workoutForResRecView = view.findViewById(R.id.workoutForResRecView);
        WorkoutsForReservationAdapter workoutsForReservationAdapter = new WorkoutsForReservationAdapter(requireActivity());
        workoutForResRecView.setAdapter(workoutsForReservationAdapter);
        workoutForResRecView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        setWorkoutPrograms(workoutsForReservationAdapter, currentDayOfWeek, authUtil, customerService);

        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            String dayOfWeekSelected = new SimpleDateFormat("EEEE", Locale.UK).format(date.getDate());
            workoutsForReservationAdapter.setDateSelected(date.getCalendar());
            //filter the availabilities by date selected
            workoutsForReservationAdapter.getFilter().filter(dayOfWeekSelected);
        });

        return view;
    }

    // gets all availabilities of workout programs and sets the Adapter
    private void setWorkoutPrograms(WorkoutsForReservationAdapter workoutsForReservationAdapter, String dayOfWeek, AuthUtil authUtil, CustomerService customerService){
        loadingData.setEnableProgressBar();

        Call<List<AvailabilityForResResponse>> call = customerService.getAllAvailabilities(authUtil.getToken());
        call.enqueue(new Callback<List<AvailabilityForResResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AvailabilityForResResponse>> call, @NonNull Response<List<AvailabilityForResResponse>> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(requireActivity(), requireActivity().getResources().getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
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
                        workoutsForReservationAdapter.setDateSelected(materialCalendarView.getSelectedDate().getCalendar());
                        workoutsForReservationAdapter.setLessonAvailabilityList(response.body());
                        workoutsForReservationAdapter.getFilter().filter(dayOfWeek);
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<List<AvailabilityForResResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }
}
