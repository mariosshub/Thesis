package com.example.gymapplication.Admin.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Admin.Fragments.AddEditWorkoutFragment;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Admin.Fragments.WorkoutAvailabilityFragment;

import java.util.ArrayList;
import java.util.List;

public class AdminWorkoutLessonsAdapter extends RecyclerView.Adapter<AdminWorkoutLessonsAdapter.ViewHolder>{
    private final Context mContext;
    private final FragmentManager fragmentManager;
    private List<WorkoutLessons> workoutLessonList = new ArrayList<>();


    public AdminWorkoutLessonsAdapter(Context mContext, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.fragmentManager = fragmentManager;
    }

    public void setWorkoutLessonList(List<WorkoutLessons> workoutList) {
        this.workoutLessonList = workoutList;
        notifyItemChanged(workoutLessonList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.admin_workout_lessons_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final WorkoutLessons currentWorkoutLesson = workoutLessonList.get(position);
        Resources res = mContext.getResources();

        holder.workoutNameText.setText(currentWorkoutLesson.getName());
        String costPerDay = res.getString(R.string.cost_per_day, String.valueOf(currentWorkoutLesson.getCostPerDay()));
        String costPerWeek = res.getString(R.string.cost_per_week, String.valueOf(currentWorkoutLesson.getCostPerWeek()));
        String costPerMonth = res.getString(R.string.cost_per_month, String.valueOf(currentWorkoutLesson.getCostPerMonth()));
        String costPerYear = res.getString(R.string.cost_per_year, String.valueOf(currentWorkoutLesson.getCostPerYear()));
        String maxSeats = res.getString(R.string.max_seats, String.valueOf(currentWorkoutLesson.getMaxPeeps()));

        holder.costPerDayText.setText(costPerDay);
        holder.costPerWeekText.setText(costPerWeek);
        holder.costPerMonthText.setText(costPerMonth);
        holder.costPerYearText.setText(costPerYear);
        holder.maxSeatsText.setText(maxSeats);

        holder.descriptionText.setText(currentWorkoutLesson.getDescription());

        // button for adding availability to workout lesson
        holder.addAvailabilityBtn.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("currentWorkout",currentWorkoutLesson);
            WorkoutAvailabilityFragment workoutAvailabilityFragment = new WorkoutAvailabilityFragment();
            workoutAvailabilityFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.fragment_container, workoutAvailabilityFragment).addToBackStack(null).commit();
        });

        // button for editing a workout lesson
        holder.editBtn.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("currentWorkout",currentWorkoutLesson);
            AddEditWorkoutFragment addEditWorkoutFragment = new AddEditWorkoutFragment();
            addEditWorkoutFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.fragment_container, addEditWorkoutFragment).addToBackStack(null).commit();
        });

    }

    @Override
    public int getItemCount() {
        return workoutLessonList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView workoutNameText, costPerDayText, costPerWeekText, costPerMonthText, costPerYearText, maxSeatsText, descriptionText;
        private Button addAvailabilityBtn, editBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }
        private void initViews(){
            workoutNameText = itemView.findViewById(R.id.workoutName);
            descriptionText = itemView.findViewById(R.id.workoutDescr);
            costPerDayText = itemView.findViewById(R.id.workoutCostPerDay);
            costPerWeekText = itemView.findViewById(R.id.workoutCostPerWeek);
            costPerMonthText = itemView.findViewById(R.id.workoutCostPerMonth);
            costPerYearText = itemView.findViewById(R.id.workoutCostPerYear);
            maxSeatsText = itemView.findViewById(R.id.workoutMaxSeats);
            addAvailabilityBtn = itemView.findViewById(R.id.addAvailability);
            editBtn = itemView.findViewById(R.id.editWorkoutBtn);
        }
    }

}
