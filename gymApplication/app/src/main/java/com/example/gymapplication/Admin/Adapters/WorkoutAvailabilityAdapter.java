package com.example.gymapplication.Admin.Adapters;

import static com.example.gymapplication.Api.ApiExceptions.AVAILABILITY_NOT_FOUND;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Model.LessonAvailability;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.example.gymapplication.Utils.RecyclerViewDataPass;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WorkoutAvailabilityAdapter extends RecyclerView.Adapter<WorkoutAvailabilityAdapter.ViewHolder> {
    private final Context mContext;
    private final RecyclerViewDataPass recyclerViewDataPass;
    private LoadingData loadingData;
    private List<LessonAvailability> lessonAvailabilityList = new ArrayList<>();

    private AdminService adminService;
    private AuthUtil authUtil;


    public WorkoutAvailabilityAdapter(Context mContext, RecyclerViewDataPass recyclerViewDataPass, LoadingData loadingData){
        this.mContext = mContext;
        this.recyclerViewDataPass = recyclerViewDataPass;
        this.loadingData = loadingData;
    }

    public void setLessonAvailabilityList(List<LessonAvailability> lessonAvailabilityList) {
        this.lessonAvailabilityList = lessonAvailabilityList;
        notifyItemChanged(lessonAvailabilityList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.workout_availability_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resources res = mContext.getResources();
        final LessonAvailability currentLessonAvailability = lessonAvailabilityList.get(position);

        //localize date text
        switch (currentLessonAvailability.getDate()){
            case "Monday":
                holder.daysTextView.setText(res.getString(R.string.monday));
                break;
            case "Tuesday":
                holder.daysTextView.setText(res.getString(R.string.tuesday));
                break;
            case "Wednesday":
                holder.daysTextView.setText(res.getString(R.string.wednesday));
                break;
            case "Thursday":
                holder.daysTextView.setText(res.getString(R.string.thursday));
                break;
            case "Friday":
                holder.daysTextView.setText(res.getString(R.string.friday));
                break;
            case "Saturday":
                holder.daysTextView.setText(res.getString(R.string.saturday));
                break;
            case "Sunday":
                holder.daysTextView.setText(res.getString(R.string.sunday));
                break;
        }
        holder.startHourTextView.setText(currentLessonAvailability.getStartingHour());
        holder.endHourTextView.setText(currentLessonAvailability.getEndHour());

        holder.removeAvailabilityBtn.setOnClickListener(v -> new AlertDialog.Builder(mContext)
                .setMessage(res.getString(R.string.availability_delete_message))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes_dialog_message), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadingData.setEnableProgressBar();

                        Call<WorkoutLessons> call = adminService.deleteLessonAvailability(authUtil.getToken(),currentLessonAvailability.getId());
                        call.enqueue(new Callback<WorkoutLessons>() {
                            @Override
                            public void onResponse(@NonNull Call<WorkoutLessons> call, @NonNull Response<WorkoutLessons> response) {
                                if(!response.isSuccessful()){
                                    if(response.code() == 403){
                                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                                        Toast.makeText(mContext, res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        if(response.errorBody() != null) {
                                            try {
                                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                                if(message.equals(AVAILABILITY_NOT_FOUND))
                                                    Toast.makeText(mContext,res.getString(R.string.availability_not_found),Toast.LENGTH_SHORT).show();
                                            }
                                            catch (Exception e){
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                    }
                                }
                                else{
                                    WorkoutLessons lesson = response.body();
                                    if(lesson != null){
                                        Toast.makeText(mContext, res.getString(R.string.availability_deleted), Toast.LENGTH_SHORT).show();
                                        lessonAvailabilityList.remove(holder.getAdapterPosition());
                                        recyclerViewDataPass.passArray(lessonAvailabilityList);
                                        notifyItemRemoved(holder.getAdapterPosition());
                                    }
                                }
                                loadingData.setDisableProgressBar();
                            }

                            @Override
                            public void onFailure(@NonNull Call<WorkoutLessons> call, @NonNull Throwable t) {
                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingData.setDisableProgressBar();
                            }
                        });
                    }
                })
                .setNegativeButton(res.getString(R.string.no_dialog_message), null)
                .show());
    }

    @Override
    public int getItemCount() {
        return lessonAvailabilityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView daysTextView, startHourTextView, endHourTextView;
        private ImageView removeAvailabilityBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            initAuth();
        }

        private void initViews(){
            daysTextView = itemView.findViewById(R.id.daysTextView);
            startHourTextView = itemView.findViewById(R.id.startHoursTextView);
            endHourTextView = itemView.findViewById(R.id.endHoursTextView);
            removeAvailabilityBtn = itemView.findViewById(R.id.removeAvailability);
        }

        private void initAuth(){
            adminService = ApiClient.getAdminService();
            authUtil = new AuthUtil(mContext);
        }
    }
}
