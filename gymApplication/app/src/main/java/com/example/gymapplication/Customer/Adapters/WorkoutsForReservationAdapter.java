package com.example.gymapplication.Customer.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Customer.Fragments.ReservationFragment;
import com.example.gymapplication.R;
import com.example.gymapplication.Response.AvailabilityForResResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WorkoutsForReservationAdapter extends RecyclerView.Adapter<WorkoutsForReservationAdapter.ViewHolder> implements Filterable {
    private final Context mContext;
    private List<AvailabilityForResResponse> lessonAvailabilityList = new ArrayList<>();
    private List<AvailabilityForResResponse> lessonAvailabilityByDayList= new ArrayList<>();
    private Calendar dateSelected;
    private Resources res;

    public WorkoutsForReservationAdapter(Context mContext){
        this.mContext = mContext;
    }

    public void setLessonAvailabilityList(List<AvailabilityForResResponse> lessonAvailabilityByDayList) {
        this.lessonAvailabilityByDayList = lessonAvailabilityByDayList;
        this.lessonAvailabilityList = new ArrayList<>(lessonAvailabilityByDayList);
    }

    public void setDateSelected(Calendar dateSelected) {
        this.dateSelected = dateSelected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.workouts_for_reservation_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        res = mContext.getResources();
        final AvailabilityForResResponse currentLessonAvailability = lessonAvailabilityByDayList.get(position);

        holder.workoutName.setText(currentLessonAvailability.getWorkoutName());

        holder.workoutAvailabilityFromTo.setText(res.getString(R.string.lesson_availability_from_to,
                currentLessonAvailability.getStartingHour(),currentLessonAvailability.getEndHour()));

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if current date and date selected is the same
                Date currentDate = new Date();
                Calendar calendarCurrentDate = Calendar.getInstance();
                calendarCurrentDate.setTime(currentDate);

                // setting time to zero we want to compare only the dates and not time
                calendarCurrentDate.set(Calendar.HOUR_OF_DAY,0);
                calendarCurrentDate.set(Calendar.MINUTE,0);
                calendarCurrentDate.set(Calendar.SECOND,0);
                calendarCurrentDate.set(Calendar.MILLISECOND,0);

                dateSelected.set(Calendar.HOUR_OF_DAY,0);
                dateSelected.set(Calendar.MINUTE,0);
                dateSelected.set(Calendar.SECOND,0);
                dateSelected.set(Calendar.MILLISECOND,0);

                if(dateSelected.after(calendarCurrentDate)){
                    Toast.makeText(mContext,res.getString(R.string.cant_book_for_next_day),Toast.LENGTH_SHORT).show();
                    return;
                }

                // fix currentDate
                calendarCurrentDate.setTime(currentDate);

                // get the ending time of selected workout
                int endTimeWorkout = Integer.parseInt(currentLessonAvailability.getEndHour().split(":")[0].replaceFirst("^0*",""));
                dateSelected.set(Calendar.HOUR_OF_DAY,endTimeWorkout-1);

                //check if workout ending time is before the current time
                if(dateSelected.before(calendarCurrentDate)){
                    Toast.makeText(mContext,res.getString(R.string.workout_ended,currentLessonAvailability.getWorkoutName()),Toast.LENGTH_SHORT).show();
                }
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("currentAvailability",currentLessonAvailability);
                    ReservationFragment reservationFragment = new ReservationFragment();
                    reservationFragment.setArguments(bundle);
                    ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, reservationFragment).addToBackStack(null).commit();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return lessonAvailabilityByDayList.size();
    }

    @Override
    public Filter getFilter() {
        return dayFilter;
    }

    //filter workout availabilities by day of week
    private final Filter dayFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AvailabilityForResResponse> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(lessonAvailabilityList);
            }
            else{
                String filterPattern = constraint.toString();
                for(AvailabilityForResResponse item : lessonAvailabilityList){
                    if(item.getDate().equals(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            lessonAvailabilityByDayList.clear();
            lessonAvailabilityByDayList.addAll((List<AvailabilityForResResponse>) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout parent;
        private TextView workoutName, workoutAvailabilityFromTo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews(){
            parent = itemView.findViewById(R.id.workoutForRes);
            workoutName = itemView.findViewById(R.id.workoutForResName);
            workoutAvailabilityFromTo = itemView.findViewById(R.id.lessonAvailableFromTo);
        }

    }
}
