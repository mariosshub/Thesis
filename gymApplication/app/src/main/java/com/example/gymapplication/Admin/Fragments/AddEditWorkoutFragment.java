package com.example.gymapplication.Admin.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.LESSON_NOT_FOUND;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Common.MainActivity;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.WorkoutLessonRequest;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditWorkoutFragment extends Fragment implements View.OnClickListener {

    private TextView header;
    private EditText workoutName, workoutCostPerDay, workoutCostPerWeek, workoutCostPerMonth, workoutCostPerYear, maxPeople, workoutDescription;
    private Button addBtn;
    private ConstraintLayout parent;

    private AuthUtil authUtil;
    private AdminService adminService;
    private WorkoutLessons currentWorkoutLesson;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_workout_fragment,container,false);
        res = requireActivity().getResources();
        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForConLayout(parent);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            currentWorkoutLesson = (WorkoutLessons) bundle.getSerializable("currentWorkout");
        }

        if(currentWorkoutLesson != null){
            header.setText(R.string.edit_workout);
            workoutName.setText(currentWorkoutLesson.getName());
            workoutCostPerDay.setText(String.valueOf(currentWorkoutLesson.getCostPerDay()));
            workoutCostPerWeek.setText(String.valueOf(currentWorkoutLesson.getCostPerWeek()));
            workoutCostPerMonth.setText(String.valueOf(currentWorkoutLesson.getCostPerMonth()));
            workoutCostPerYear.setText(String.valueOf(currentWorkoutLesson.getCostPerYear()));
            maxPeople.setText(String.valueOf(currentWorkoutLesson.getMaxPeeps()));
            workoutDescription.setText(currentWorkoutLesson.getDescription());
            addBtn.setText(R.string.edit_lesson_btn);
        }

        addBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addWorkoutBtn) {
            addNewWorkout();
        }
    }

    private void addNewWorkout(){
        String name = workoutName.getText().toString();
        String day = workoutCostPerDay.getText().toString();
        String week = workoutCostPerWeek.getText().toString();
        String month = workoutCostPerMonth.getText().toString();
        String year = workoutCostPerYear.getText().toString();
        String maxPeeps = maxPeople.getText().toString();
        String description = workoutDescription.getText().toString();

        if(name.isEmpty()){
            workoutName.setError(res.getString(R.string.no_empty_name_workout));
            return;
        }
        else if(day.isEmpty()){
            workoutCostPerDay.setError(res.getString(R.string.no_empty_cost_workout));
            return;
        }
        else if(week.isEmpty()){
            workoutCostPerWeek.setError(res.getString(R.string.no_empty_cost_workout));
            return;
        }
        else if(month.isEmpty()){
            workoutCostPerMonth.setError(res.getString(R.string.no_empty_cost_workout));
            return;
        }
        else if(year.isEmpty()){
            workoutCostPerYear.setError(res.getString(R.string.no_empty_cost_workout));
            return;
        }
        else if(maxPeeps.isEmpty()){
            maxPeople.setError(res.getString(R.string.no_empty_people_workout));
            return;
        }

        // edit the workout lesson
        if(currentWorkoutLesson != null){
            WorkoutLessonRequest workoutLessonRequest = new WorkoutLessonRequest(currentWorkoutLesson.getId(),name,description,Double.parseDouble(day),Double.parseDouble(week),Double.parseDouble(month),Double.parseDouble(year),Long.parseLong(maxPeeps));

            loadingData.setEnableProgressBar();

            Call<WorkoutLessons> call = adminService.editWorkoutLesson(authUtil.getToken(), workoutLessonRequest);
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
                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                    if(message.equals(LESSON_NOT_FOUND))
                                        Toast.makeText(requireActivity(),res.getString(R.string.lesson_not_found),Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        loadingData.setDisableProgressBar();
                    }
                    else{
                        Toast.makeText(requireActivity(), res.getString(R.string.workout_edited), Toast.LENGTH_SHORT).show();
                        loadingData.setDisableProgressBar();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<WorkoutLessons> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });

        }
        // add a new workout lesson
        else{
            WorkoutLessonRequest workoutLesson = new WorkoutLessonRequest(name,description,Double.parseDouble(day),Double.parseDouble(week),Double.parseDouble(month),Double.parseDouble(year),Long.parseLong(maxPeeps));

            Call<WorkoutLessons> call = adminService.addWorkoutLesson(authUtil.getToken(), workoutLesson);
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
                        loadingData.setDisableProgressBar();
                    }
                    else{
                        WorkoutLessons workoutLesson = response.body();
                        Toast.makeText(requireActivity(), res.getString(R.string.workout_added), Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("currentWorkout",workoutLesson);
                        WorkoutAvailabilityFragment workoutAvailabilityFragment = new WorkoutAvailabilityFragment();
                        workoutAvailabilityFragment.setArguments(bundle);
                        loadingData.setDisableProgressBar();
                        requireActivity().getSupportFragmentManager().popBackStack();
                        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, workoutAvailabilityFragment).addToBackStack(null).commit();

                    }
                }

                @Override
                public void onFailure(@NonNull Call<WorkoutLessons> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }

    }

    private void initViews(View view){
        parent = view.findViewById(R.id.add_edit_workout);
        header = view.findViewById(R.id.textHeaderAddWorkout);
        workoutName = view.findViewById(R.id.editTextWorkoutName);
        workoutCostPerDay = view.findViewById(R.id.editTextCostPerDay);
        workoutCostPerWeek = view.findViewById(R.id.editTextCostPerWeek);
        workoutCostPerMonth = view.findViewById(R.id.editTextCostPerMonth);
        workoutCostPerYear = view.findViewById(R.id.editTextCostPerYear);
        maxPeople = view.findViewById(R.id.maxPeople);
        workoutDescription = view.findViewById(R.id.editTextWorkoutDescription);
        addBtn = view.findViewById(R.id.addWorkoutBtn);
    }
}
