package com.example.gymapplication.Customer.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.SUB_CUSTOMER_NOT_FOUND;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Common.MainActivity;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.Subscription;
import com.example.gymapplication.Model.SubscriptionWorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerSubscriptionFragment extends Fragment {
    private TextView subsHeader;
    private LinearLayout subsLayout;
    private RelativeLayout parent;

    private Subscription customersSub ;
    private CustomerService customerService;
    private AuthUtil authUtil;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subscriptions_fragment,container,false);
        res = requireActivity().getResources();
        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        customerService = ApiClient.getCustomerService();
        authUtil = new AuthUtil(requireActivity());

        findSub();

        return view;
    }

    private void findSub(){
        loadingData.setEnableProgressBar();

        Call<Subscription> call = customerService.getSub(authUtil.getToken());
        call.enqueue(new Callback<Subscription>() {
            @Override
            public void onResponse(@NonNull Call<Subscription> call, @NonNull Response<Subscription> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(requireActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                if(message.equals(SUB_CUSTOMER_NOT_FOUND))
                                    subsHeader.setText(res.getString(R.string.sub_cust_not_found));
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                else {
                    if(response.body() != null){
                        customersSub = response.body();
                        addSubView();
                    }
                }
                loadingData.setDisableProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<Subscription> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

    private void addSubView(){
        View subView = LayoutInflater.from(requireActivity()).inflate(R.layout.subscriptions_list, null, false);

        TextView customerSub, subTotalCost;

        LinearLayout lessonsLayout = subView.findViewById(R.id.layout_lessons_list);
        customerSub = subView.findViewById(R.id.customerSubName);
        subTotalCost = subView.findViewById(R.id.subCost);

        customerSub.setText(customersSub.getCustomer().getName());
        subTotalCost.setText(String.valueOf(customersSub.getCost()));

        List<SubscriptionWorkoutLessons> subWorkoutLessonsList = customersSub.getSubscriptionWorkoutLessons();

        if(subWorkoutLessonsList.size() > 0){
            for (SubscriptionWorkoutLessons subWorkoutLesson:subWorkoutLessonsList) {
                addSubLessonsView(subWorkoutLesson, lessonsLayout);
            }
        }

        subsLayout.addView(subView);
    }

    private void addSubLessonsView(SubscriptionWorkoutLessons subWorkoutLesson,LinearLayout lessonsLayout){
        final View lessonsView = LayoutInflater.from(requireActivity()).inflate(R.layout.subscription_lesson_row,null,false);

        TextView lessonName, expired, createdAt, expiresAt, cost, editLessonBtn;

        lessonName = lessonsView.findViewById(R.id.subLessonName);
        expired = lessonsView.findViewById(R.id.subLessonExpired);
        createdAt = lessonsView.findViewById(R.id.subLessonCreatedAt);
        expiresAt = lessonsView.findViewById(R.id.subLessonExpiresAt);
        cost = lessonsView.findViewById(R.id.subLessonCost);

        lessonName.setText(subWorkoutLesson.getWorkoutLesson().getName());

        if(subWorkoutLesson.isExpired()){
            expired.setVisibility(View.VISIBLE);
        }

        createdAt.setText(res.getString(R.string.sub_created_at,subWorkoutLesson.getCreatedAt()));
        expiresAt.setText(res.getString(R.string.sub_expires_at,subWorkoutLesson.getExpiresAt()));
        cost.setText(requireActivity().getString(R.string.sub_lesson_cost,String.valueOf(subWorkoutLesson.getLessonCost())));

        lessonsLayout.addView(lessonsView);
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.subs_fragment);
        subsHeader = view.findViewById(R.id.subscriptions_header);
        subsHeader.setText(R.string.customersSubHeader);
        Button addSubBtn = view.findViewById(R.id.addSub);
        addSubBtn.setVisibility(View.GONE);
        subsLayout = view.findViewById(R.id.layout_subs_list);
    }
}
