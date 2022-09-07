package com.example.gymapplication.Customer.Adapters;

import static com.example.gymapplication.Api.ApiExceptions.RESERVATION_CANCELED;
import static com.example.gymapplication.Api.ApiExceptions.RESERVATION_MADE;
import static com.example.gymapplication.Api.ApiExceptions.RESERVATION_NOT_FOUND;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.Reservations;
import com.example.gymapplication.Model.Status;
import com.example.gymapplication.R;
import com.example.gymapplication.Broadcast.ReminderBroadcast;
import com.example.gymapplication.Response.ReservationsResponse;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowAllReservationsAdapter extends RecyclerView.Adapter<ShowAllReservationsAdapter.ViewHolder> implements Filterable {
    private final Context mContext;
    private List<ReservationsResponse> reservationsList = new ArrayList<>();
    private List<ReservationsResponse> reservationsListFiltered = new ArrayList<>();
    private Resources res;
    private final LoadingData loadingData;

    public ShowAllReservationsAdapter(Context mContext, LoadingData loadingData) {
        this.mContext = mContext;
        this.loadingData = loadingData;
    }

    public void setReservationsList(List<ReservationsResponse> reservationsListFiltered) {
        this.reservationsListFiltered =reservationsListFiltered;
        this.reservationsList = new ArrayList<>(reservationsListFiltered);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.all_reservations_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        res = mContext.getResources();
        final ReservationsResponse currentReservation = reservationsListFiltered.get(position);

        // change color alternatively
        if(position % 2 == 0){
            holder.parent.setBackgroundColor(res.getColor(R.color.gray_blue, mContext.getTheme()));
        }

        holder.date.setText(currentReservation.getDate());
        holder.dayOfWeek.setText(currentReservation.getDayOfWeek());
        holder.hour.setText(currentReservation.getHour());
        holder.workoutName.setText(currentReservation.getWorkoutName());

        switch (currentReservation.getStatus()){
            case BOOKED:
                holder.status.setText(res.getString(R.string.sub_lesson_status_booked));
                holder.status.setTextColor(res.getColor(R.color.green, mContext.getTheme()));
                holder.cancelBtn.setVisibility(View.VISIBLE);
                break;
            case CANCELLED:
                holder.status.setText(res.getString(R.string.sub_lesson_status_canceled));
                holder.status.setTextColor(res.getColor(R.color.red, mContext.getTheme()));
                holder.cancelBtn.setVisibility(View.GONE);
                break;
            case CONFIRMED:
                holder.status.setText(res.getString(R.string.sub_lesson_status_confirmed));
                holder.status.setTextColor(res.getColor(R.color.blue, mContext.getTheme()));
                holder.cancelBtn.setVisibility(View.GONE);
                break;
        }

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,0,intent,0);
                AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                //cancel the pending alarm
                alarmManager.cancel(pendingIntent);

                cancelReservation(currentReservation.getReservationId(),holder.getAdapterPosition(), currentReservation);
            }
        });
    }

    private void cancelReservation(Long resId, int position, ReservationsResponse currentRes){
        CustomerService customerService = ApiClient.getCustomerService();
        AuthUtil authUtil = new AuthUtil(mContext);

        loadingData.setEnableProgressBar();

        Call<Boolean> call = customerService.cancelReservation(authUtil.getToken(),resId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(mContext, res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                if(message.equals(RESERVATION_NOT_FOUND))
                                    Toast.makeText(mContext,res.getString(R.string.res_not_found),Toast.LENGTH_SHORT).show();
                                else if(message.equals(RESERVATION_CANCELED))
                                    Toast.makeText(mContext,res.getString(R.string.res_canceled),Toast.LENGTH_SHORT).show();
                                else if(message.equals(RESERVATION_MADE))
                                    Toast.makeText(mContext,res.getString(R.string.res_made),Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                else{
                    if(response.body() != null && response.body()){
                        Toast.makeText(mContext,res.getString(R.string.reservation_canceled),Toast.LENGTH_SHORT).show();
                        currentRes.setStatus(Status.CANCELLED);
                        reservationsListFiltered.set(position,currentRes);
                        notifyItemChanged(position);
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Toast.makeText(mContext,t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationsListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return resFilter;
    }

    //filter the list of reservations by current date
    private final Filter resFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ReservationsResponse> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(reservationsList);
            }
            else {
                LocalDate resDate = null;
                for (ReservationsResponse item : reservationsList) {
                    try {
                        resDate = LocalDate.parse(item.getDate());
                    }
                    catch (DateTimeException e){
                        System.out.println(e.getMessage());
                    }
                    if(resDate != null){
                        if(LocalDate.now().equals(resDate)){
                            filteredList.add(item);
                        }
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            reservationsListFiltered.clear();
            reservationsListFiltered.addAll((List<ReservationsResponse>) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout parent;
        private TextView date, dayOfWeek,hour,workoutName,status,cancelBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews(){
            parent = itemView.findViewById(R.id.allResLayout);
            date = itemView.findViewById(R.id.allResDate);
            dayOfWeek = itemView.findViewById(R.id.allResDayOfWeek);
            hour = itemView.findViewById(R.id.allResHour);
            workoutName = itemView.findViewById(R.id.allResLessonName);
            status = itemView.findViewById(R.id.allResStatus);
            cancelBtn = itemView.findViewById(R.id.allResCancel);

        }
    }
}

