package com.example.gymapplication.Admin.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.SUB_NOT_FOUND;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Common.MainActivity;
import com.example.gymapplication.Model.Subscription;
import com.example.gymapplication.Model.SubscriptionWorkoutLessons;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.EditSubRequest;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.EditSubPassEmail;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionsFragment extends Fragment implements View.OnClickListener{
    private Button addSubBtn;
    private Subscription currentSub ;
    private AdminService adminService;
    private AuthUtil authUtil;
    private LinearLayout subsLayout;
    private View subView;
    private RelativeLayout parent;

    private EditSubPassEmail editSubPassEmail;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subscriptions_fragment,container,false);
        res = requireActivity().getResources();
        setHasOptionsMenu(true);
        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        addSubBtn.setOnClickListener(this);

        adminService = ApiClient.getAdminService();
        authUtil = new AuthUtil(requireActivity());

        editSubPassEmail = new EditSubPassEmail() {
            @Override
            public void passEmailSub(String subsEmail) {
                if(subsEmail != null){
                    findSub(subsEmail);
                }
            }
        };

        return view;
    }

    private void addSubsView(){
        subView = LayoutInflater.from(requireActivity()).inflate(R.layout.subscriptions_list,null,false);

        TextView customerSub, subTotalCost;

        LinearLayout lessonsLayout = subView.findViewById(R.id.layout_lessons_list);
        customerSub = subView.findViewById(R.id.customerSubName);
        subTotalCost = subView.findViewById(R.id.subCost);

        customerSub.setText(currentSub.getCustomer().getName());
        subTotalCost.setText(String.valueOf(currentSub.getCost()));

        List<SubscriptionWorkoutLessons> subWorkoutLessonsList = currentSub.getSubscriptionWorkoutLessons();

        if(subWorkoutLessonsList.size() > 0){
            for (SubscriptionWorkoutLessons subWorkoutLesson:subWorkoutLessonsList) {
                addSubLessonsView(subWorkoutLesson,currentSub.getId(), lessonsLayout);
            }
        }

        subsLayout.addView(subView);
    }

    private void addSubLessonsView(SubscriptionWorkoutLessons subWorkoutLesson, Long subId, LinearLayout lessonsLayout){
        final View lessonsView = LayoutInflater.from(requireActivity()).inflate(R.layout.subscription_lesson_row,null,false);

        TextView lessonName, expired, createdAt, expiresAt, cost, editLessonBtn;

        lessonName = lessonsView.findViewById(R.id.subLessonName);
        expired = lessonsView.findViewById(R.id.subLessonExpired);
        createdAt = lessonsView.findViewById(R.id.subLessonCreatedAt);
        expiresAt = lessonsView.findViewById(R.id.subLessonExpiresAt);
        cost = lessonsView.findViewById(R.id.subLessonCost);
        editLessonBtn = lessonsView.findViewById(R.id.subLessonEdit);

        lessonName.setText(subWorkoutLesson.getWorkoutLesson().getName());

        if(subWorkoutLesson.isExpired()){
            expired.setVisibility(View.VISIBLE);
            editLessonBtn.setVisibility(View.VISIBLE);
            editLessonBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(requireActivity(),res.getString(R.string.sub_update_lesson,subWorkoutLesson.getWorkoutLesson().getName()), Toast.LENGTH_SHORT).show();

                    EditSubRequest editSubRequest = new EditSubRequest(subId,subWorkoutLesson.getId(),String.valueOf(subWorkoutLesson.getCost()),subWorkoutLesson.getAmount());
                    AddEditSubDialog addEditSubDialog = new AddEditSubDialog(getContext(),editSubPassEmail, editSubRequest);
                    addEditSubDialog.show(requireActivity().getSupportFragmentManager(), "update sub dialog");
                }
            });
        }
        createdAt.setText(res.getString(R.string.sub_created_at,subWorkoutLesson.getCreatedAt()));
        expiresAt.setText(res.getString(R.string.sub_expires_at,subWorkoutLesson.getExpiresAt()));
        cost.setText(requireActivity().getString(R.string.sub_lesson_cost,String.valueOf(subWorkoutLesson.getLessonCost())));

        lessonsLayout.addView(lessonsView);
    }

    private void findSub(String email){
        loadingData.setEnableProgressBar();

        Call<Subscription> call = adminService.findSubByEmail(authUtil.getToken(), email);
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
                                if(message.equals(SUB_NOT_FOUND))
                                    Toast.makeText(requireActivity(),res.getString(R.string.sub_not_found),Toast.LENGTH_SHORT).show();
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
                        currentSub = sub;
                        subsLayout.removeView(subView);
                        addSubsView();
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint(res.getString(R.string.email_example));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println(query);
                subsLayout.removeView(subView);
                findSub(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.subs_fragment);
        addSubBtn = view.findViewById(R.id.addSub);
        subsLayout = view.findViewById(R.id.layout_subs_list);
    }

    @Override
    public void onClick(View v) {
        AddEditSubDialog addEditSubDialog = new AddEditSubDialog(requireActivity(),editSubPassEmail);
        addEditSubDialog.show(requireActivity().getSupportFragmentManager(), "add sub dialog");
    }

}
