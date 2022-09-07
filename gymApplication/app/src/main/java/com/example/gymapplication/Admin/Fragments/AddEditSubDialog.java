package com.example.gymapplication.Admin.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.CUSTOMERS_EMAIL_NOT_FOUND;
import static com.example.gymapplication.Api.ApiExceptions.LESSONS_SUB_NOT_FOUND;
import static com.example.gymapplication.Api.ApiExceptions.SUBSCRIPTION_EXISTS;
import static com.example.gymapplication.Api.ApiExceptions.SUBSCRIPTION_NOT_EXPIRED;
import static com.example.gymapplication.Api.ApiExceptions.SUB_NOT_FOUND;
import static com.example.gymapplication.Api.ApiExceptions.WORKOUT_NOT_FOUND;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Model.CostConstant;
import com.example.gymapplication.Model.Subscription;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.AddSubRequest;
import com.example.gymapplication.Request.EditSubRequest;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.EditSubPassEmail;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditSubDialog extends AppCompatDialogFragment {

    private EditText email, amount;
    private Spinner workoutLessonsSpinner, costTypeSpinner;
    private AdminService adminService;
    private AuthUtil authUtil;
    private List<WorkoutLessons> workoutLessons = new ArrayList<>();

    private final Context context;
    private EditSubRequest editSubRequest;
    private final EditSubPassEmail editSubPassEmail;
    private Resources res;
    private LoadingData loadingData;

    public AddEditSubDialog(Context context, EditSubPassEmail editSubPassEmail, EditSubRequest editSubRequest) {
        this.context = context;
        this.editSubPassEmail = editSubPassEmail;
        this.editSubRequest = editSubRequest;
    }

    public AddEditSubDialog(Context context, EditSubPassEmail editSubPassEmail){
        this.context = context;
        this.editSubPassEmail = editSubPassEmail;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        res = context.getResources();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_edit_sub_dialog,null);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        initViews(view);

        // dialog for adding a subscription
        if(editSubRequest == null){
            builder.setView(view).setTitle(res.getString(R.string.add_dialog_title))
                    .setNegativeButton(res.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton(res.getString(R.string.add_btn), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String customersMail = email.getText().toString();
                            WorkoutLessons workoutLesson = (WorkoutLessons) workoutLessonsSpinner.getSelectedItem();
                            int pos = costTypeSpinner.getSelectedItemPosition();

                            String cost = "";
                            switch (pos){
                                case 0:
                                    cost = CostConstant.COSTPERDAY;
                                    break;
                                case 1:
                                    cost =  CostConstant.COSTPERWEEK;
                                    break;
                                case 2:
                                    cost =  CostConstant.COSTPERMONTH;
                                    break;
                                case 3:
                                    cost = CostConstant.COSTPERYEAR;
                                    break;
                            }
                            String costAmountString = amount.getText().toString();

                            if(customersMail.isEmpty()){
                                email.setError(res.getString(R.string.no_empty_fields));
                                return;
                            }
                            else if(costAmountString.isEmpty()){
                                amount.setError(res.getString(R.string.no_empty_fields));
                                return;
                            }

                            int costAmount = Integer.parseInt(amount.getText().toString());

                            AddSubRequest addSubRequest = new AddSubRequest(customersMail,workoutLesson.getId(),cost,costAmount);
                            loadingData.setEnableProgressBar();

                            Call<Subscription> call = adminService.addSubToCustomer(authUtil.getToken(), addSubRequest);
                            call.enqueue(new Callback<Subscription>() {
                                @Override
                                public void onResponse(@NonNull Call<Subscription> call, @NonNull Response<Subscription> response) {
                                    if(!response.isSuccessful()){
                                        if(response.code() == 403){
                                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                                            Toast.makeText(context, res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            if(response.errorBody() != null) {
                                                try {
                                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                                    switch (message) {
                                                        case CUSTOMERS_EMAIL_NOT_FOUND:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.cust_email_not_found, customersMail), Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case WORKOUT_NOT_FOUND:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.workout_not_found, workoutLesson.getName()), Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SUBSCRIPTION_EXISTS:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.sub_exists), Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                                catch (Exception e){
                                                    System.out.println(e.getMessage());
                                                }
                                            }
                                        }
                                    }
                                    else{
                                        Subscription sub = response.body();
                                        if(sub != null){
                                            Toast.makeText(context, res.getString(R.string.sub_added_success) ,Toast.LENGTH_SHORT).show();
                                            editSubPassEmail.passEmailSub(sub.getCustomer().getEmail());
                                        }
                                    }
                                    loadingData.setDisableProgressBar();
                                }

                                @Override
                                public void onFailure(@NonNull Call<Subscription> call, @NonNull Throwable t) {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingData.setDisableProgressBar();
                                }
                            });
                        }
                    });
        }
        // dialog for editing existing subscription that has expired
        else{
            builder.setView(view).setTitle(res.getString(R.string.edit_dialog_tittle))
                    .setNegativeButton(res.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton(res.getString(R.string.update), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int pos = costTypeSpinner.getSelectedItemPosition();
                            String cost = "";
                            switch (pos){
                                case 0:
                                    cost = CostConstant.COSTPERDAY;
                                    break;
                                case 1:
                                    cost =  CostConstant.COSTPERWEEK;
                                    break;
                                case 2:
                                    cost =  CostConstant.COSTPERMONTH;
                                    break;
                                case 3:
                                    cost = CostConstant.COSTPERYEAR;
                                    break;
                            }

                            String costAmountString = amount.getText().toString();
                            if(costAmountString.isEmpty()){
                                amount.setError(res.getString(R.string.no_empty_fields));
                                return;
                            }

                            int costAmount = Integer.parseInt(costAmountString);

                            editSubRequest.setCost(cost);
                            editSubRequest.setAmount(costAmount);


                            loadingData.setEnableProgressBar();
                            Call<Subscription> call = adminService.updateSub(authUtil.getToken(),editSubRequest);
                            call.enqueue(new Callback<Subscription>() {
                                @Override
                                public void onResponse(@NonNull Call<Subscription> call, @NonNull Response<Subscription> response) {
                                    if(!response.isSuccessful()){
                                        if(response.code() == 403){
                                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                                            Toast.makeText(context, res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            if(response.errorBody() != null) {
                                                try {
                                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                                    switch (message) {
                                                        case SUB_NOT_FOUND:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.sub_not_found), Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case LESSONS_SUB_NOT_FOUND:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.lesson_sub_not_found), Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SUBSCRIPTION_NOT_EXPIRED:
                                                            Toast.makeText(requireActivity(), res.getString(R.string.sub_not_expired), Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                                catch (Exception e){
                                                    System.out.println(e.getMessage());
                                                }
                                            }
                                        }
                                    }
                                    else{
                                        Subscription sub = response.body();
                                        if(sub != null){
                                            Toast.makeText(context,res.getString(R.string.sub_updated_success),Toast.LENGTH_SHORT).show();
                                            editSubPassEmail.passEmailSub(sub.getCustomer().getEmail());
                                        }
                                    }
                                    loadingData.setDisableProgressBar();
                                }

                                @Override
                                public void onFailure(@NonNull Call<Subscription> call, @NonNull Throwable t) {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingData.setDisableProgressBar();
                                }
                            });
                        }
                    });
        }

        return builder.create();
    }

    private void initViews(View view){
        RelativeLayout parent = view.findViewById(R.id.add_edit_sub_dialog);
        email = view.findViewById(R.id.addSubEmail);
        TextView selectWorkout = view.findViewById(R.id.selectWorkout);
        workoutLessonsSpinner = view.findViewById(R.id.addSubWorkouts);
        costTypeSpinner = view.findViewById(R.id.addSubCost);
        amount = view.findViewById(R.id.addSubAmount);

        loadingData = new LoadingData(context);
        loadingData.setProgressBarForRelLayout(parent);

        final List<String> enumCostList = new CostConstant(context).getListOfCosts();
        costTypeSpinner.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_item, enumCostList));

        if(editSubRequest == null){
            loadingData.setEnableProgressBar();

            // get all existing workout lessons
            Call<List<WorkoutLessons>> call = adminService.getAllLessons(authUtil.getToken());
            call.enqueue(new Callback<List<WorkoutLessons>>() {
                @Override
                public void onResponse(@NonNull Call<List<WorkoutLessons>> call, @NonNull Response<List<WorkoutLessons>> response) {
                    if(!response.isSuccessful()){
                        if(response.code() == 403){
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                            Toast.makeText(context, res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(response.errorBody() != null) {
                                try {
                                    Toast.makeText(context,new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    }
                    else{
                        workoutLessons = response.body();
                        if(workoutLessons != null){
                            if(workoutLessons.size() == 0){
                                String[] workoutNames = new String[]{res.getString(R.string.sub_no_workouts)};
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, workoutNames);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                workoutLessonsSpinner.setAdapter(arrayAdapter);
                            }
                            else{
                                //fill the workout lessons spinner with the workoutLessons List
                                ArrayAdapter<WorkoutLessons> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, workoutLessons);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                workoutLessonsSpinner.setAdapter(arrayAdapter);
                            }

                        }
                    }
                    loadingData.setDisableProgressBar();
                }

                @Override
                public void onFailure(@NonNull Call<List<WorkoutLessons>> call, @NonNull Throwable t) {
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // for updating the chosen lesson's subscription
        else{
            email.setVisibility(View.GONE);
            selectWorkout.setVisibility(View.GONE);
            workoutLessonsSpinner.setVisibility(View.GONE);
            switch (editSubRequest.getCost()){
                case "CPD":
                    costTypeSpinner.setSelection(0);
                    break;
                case "CPW":
                    costTypeSpinner.setSelection(1);
                    break;
                case "CPM":
                    costTypeSpinner.setSelection(2);
                    break;
                case "CPY":
                    costTypeSpinner.setSelection(3);
                    break;
            }

            amount.setText(String.valueOf(editSubRequest.getAmount()));
        }
    }
}
