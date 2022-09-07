package com.example.gymapplication.Admin.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Admin.Adapters.WorkoutAvailabilityAdapter;
import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Model.DaysOfWeekConstant;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.LessonAvailability;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.example.gymapplication.Utils.RecyclerViewDataPass;
import com.example.gymapplication.Request.LessonAvailabilityRequest;
import com.example.gymapplication.Service.AdminService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutAvailabilityFragment extends Fragment implements View.OnClickListener {
    private TextView workoutName;
    private Spinner daysSpinner;
    private NumberPicker startHourPicker, endHourPicker;
    private ImageView addAvailabilityBtn;
    private RelativeLayout parent;
    private List<LessonAvailability> lessonAvailabilityList = new ArrayList<>();
    private WorkoutLessons currentWorkoutLesson;
    private String[] hoursArray ;

    private WorkoutAvailabilityAdapter workoutAvailabilityAdapter;

    private AdminService adminService;
    private AuthUtil authUtil;
    private Resources res;
    private LoadingData loadingData;

    public void setCurrentWorkoutLesson(WorkoutLessons currentWorkoutLesson) {
        this.currentWorkoutLesson = currentWorkoutLesson;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(lessonAvailabilityList.size() == 0 ){
                    Toast.makeText(requireActivity(),res.getString(R.string.add_availability_warning), Toast.LENGTH_SHORT).show();
                }
                else{
                    this.setEnabled(false);
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workout_availability_fragment,container,false);
        res = requireActivity().getResources();
        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            currentWorkoutLesson = (WorkoutLessons) bundle.getSerializable("currentWorkout");
        }
        if(currentWorkoutLesson != null){
            workoutName.setText(currentWorkoutLesson.getName());
            if(currentWorkoutLesson.getLessonAvailabilities() != null){
                lessonAvailabilityList = currentWorkoutLesson.getLessonAvailabilities();
            }
        }

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        RecyclerViewDataPass recyclerViewDataPass = new RecyclerViewDataPass() {
            @Override
            public void passArray(List<LessonAvailability> recyclerLessonAvailabilityList) {
                lessonAvailabilityList = recyclerLessonAvailabilityList;
            }
        };
        RecyclerView workoutAvailabilityRecView = view.findViewById(R.id.availabilityRecView);
        workoutAvailabilityAdapter = new WorkoutAvailabilityAdapter(requireActivity(), recyclerViewDataPass, loadingData);
        workoutAvailabilityRecView.setAdapter(workoutAvailabilityAdapter);
        workoutAvailabilityRecView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        workoutAvailabilityAdapter.setLessonAvailabilityList(lessonAvailabilityList);

        addAvailabilityBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addAvailabilityBtn) {
            addAvailability();
        }
    }

    private void addAvailability(){
        int dayPos = daysSpinner.getSelectedItemPosition();
        String day = "";
        String dayLocalized = "";
        switch (dayPos){
            case 0:
                day = DaysOfWeekConstant.MONDAY;
                dayLocalized = res.getString(R.string.monday);
                break;
            case 1:
                day = DaysOfWeekConstant.TUESDAY;
                dayLocalized = res.getString(R.string.tuesday);
                break;
            case 2:
                day = DaysOfWeekConstant.WEDNESDAY;
                dayLocalized = res.getString(R.string.wednesday);
                break;
            case 3:
                day = DaysOfWeekConstant.THURSDAY;
                dayLocalized = res.getString(R.string.thursday);
                break;
            case 4:
                day = DaysOfWeekConstant.FRIDAY;
                dayLocalized = res.getString(R.string.friday);
                break;
            case 5:
                day = DaysOfWeekConstant.SATURDAY;
                dayLocalized = res.getString(R.string.saturday);
                break;
            case 6:
                day = DaysOfWeekConstant.SUNDAY;
                dayLocalized = res.getString(R.string.sunday);
                break;
        }
        int startHour = startHourPicker.getValue();
        int endHour = endHourPicker.getValue();

        if(endHour - startHour <= 0){
            Toast.makeText(requireActivity(),res.getString(R.string.hour_error_message) ,Toast.LENGTH_SHORT).show();
            return;
        }

        boolean dayExists = false;
        for (LessonAvailability availability : lessonAvailabilityList){
            if(availability.getDate().equals(day)){
                dayExists = true;
                break;
            }
        }

        if(dayExists){
            Toast.makeText(requireActivity(),res.getString(R.string.availability_exists_message,dayLocalized),Toast.LENGTH_SHORT).show();
            return;
        }

        LessonAvailabilityRequest request = new LessonAvailabilityRequest(day,hoursArray[startHour - 1],hoursArray[endHour -1 ],currentWorkoutLesson);

        loadingData.setEnableProgressBar();
        Call<WorkoutLessons> call = adminService.addLessonAvailability(authUtil.getToken(),request);
        call.enqueue(new Callback<WorkoutLessons>() {
            @Override
            public void onResponse(@NonNull Call<WorkoutLessons> call, @NonNull Response<WorkoutLessons> response) {
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
                    WorkoutLessons workoutLesson = response.body();
                    if(workoutLesson != null){
                        Toast.makeText(requireActivity(), res.getString(R.string.availability_inserted_message), Toast.LENGTH_SHORT).show();
                        setCurrentWorkoutLesson(workoutLesson);
                        //update this list
                        lessonAvailabilityList = workoutLesson.getLessonAvailabilities();
                        //update adapters list
                        workoutAvailabilityAdapter.setLessonAvailabilityList(workoutLesson.getLessonAvailabilities());
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<WorkoutLessons> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });

    }


    private void initViews(View view){
        parent = view.findViewById(R.id.workout_availability_fragment);
        workoutName = view.findViewById(R.id.workoutAvailabilityName);
        addAvailabilityBtn = view.findViewById(R.id.addAvailabilityBtn);
        daysSpinner = view.findViewById(R.id.daysSpinner);
        startHourPicker = view.findViewById(R.id.startHourPicker);
        endHourPicker = view.findViewById(R.id.endHourPicker);

        List<String> days = new DaysOfWeekConstant(requireActivity()).getListOfDays();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, days);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(arrayAdapter);

        //create 24 hours string values for number picker
        hoursArray = new String[24];
        for (int i=1; i <= 24; i++){
            if(i <= 9){
                hoursArray[i-1] = String.format("%02d:00", i);
            }
            else{
                hoursArray[i-1] = String.format("%d:00", i);
            }
        }
        startHourPicker.setMinValue(1);
        startHourPicker.setMaxValue(hoursArray.length - 1);
        startHourPicker.setDisplayedValues(hoursArray);

        endHourPicker.setMinValue(1);
        endHourPicker.setMaxValue(hoursArray.length - 1);
        endHourPicker.setDisplayedValues(hoursArray);
    }
}
