package com.example.gymapplication.Admin.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Admin.Adapters.AdminWorkoutLessonsAdapter;
import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminWorkoutLessonsFragment extends Fragment implements View.OnClickListener {
    private List<WorkoutLessons> workoutLessonsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_workout_lessons_fragment,container,false);

        TextView addButton = view.findViewById(R.id.addWorkoutLesson);
        RelativeLayout parent = view.findViewById(R.id.admin_workout_lessons);

        LoadingData loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        addButton.setOnClickListener(this);

        RecyclerView adminWorkoutLessonsRecView = view.findViewById(R.id.workoutLessonsRecView);

        AdminWorkoutLessonsAdapter workoutLessonsAdapter = new AdminWorkoutLessonsAdapter(requireActivity(), requireActivity().getSupportFragmentManager());
        adminWorkoutLessonsRecView.setAdapter(workoutLessonsAdapter);
        adminWorkoutLessonsRecView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        AdminService adminService = ApiClient.getAdminService();
        AuthUtil authUtil = new AuthUtil(requireActivity());

        loadingData.setEnableProgressBar();
        Call<List<WorkoutLessons>> call = adminService.getAllLessons(authUtil.getToken());
        call.enqueue(new Callback<List<WorkoutLessons>>() {
            @Override
            public void onResponse(@NonNull Call<List<WorkoutLessons>> call, @NonNull Response<List<WorkoutLessons>> response) {
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
                        workoutLessonsList = response.body();
                        workoutLessonsAdapter.setWorkoutLessonList(workoutLessonsList);
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<List<WorkoutLessons>> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addWorkoutLesson) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddEditWorkoutFragment()).addToBackStack(null).commit();
        }
    }

}
