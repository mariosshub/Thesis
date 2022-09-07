package com.example.gymapplication.Common;

import static com.example.gymapplication.Api.ApiExceptions.USER_NOT_FOUND;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Model.Role;
import com.example.gymapplication.R;
import com.example.gymapplication.Request.ProfileEditRequest;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment implements View.OnClickListener {
    private EditText name, surname, email, phone;
    private Button editBtn;
    private ConstraintLayout parent;
    private AuthUtil authUtil;
    private Customer user;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment,container,false);
        res = requireActivity().getResources();

        initViews(view);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForConLayout(parent);

        editBtn.setOnClickListener(this);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            user = (Customer) bundle.getSerializable("editUser");
        }

        if(user != null){
            setUsersFields();
        }

        authUtil = new AuthUtil(requireActivity());

        return view;
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.edit_profile);
        name = view.findViewById(R.id.editTextEditName);
        surname = view.findViewById(R.id.editTextEditSurname);
        email = view.findViewById(R.id.editTextEditEmail);
        phone = view.findViewById(R.id.editTextEditPhone);
        editBtn = view.findViewById(R.id.buttonEditProfile);
    }

    private void setUsersFields(){
        name.setText(user.getName());
        surname.setText(user.getSurname());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonEditProfile) {
            editProfile();
        }
    }
    private void editProfile(){
        String editName = name.getText().toString().trim();
        String editSurname = surname.getText().toString().trim();
        String editEmail = email.getText().toString().trim();
        String editPhone = phone.getText().toString().trim();

        if(editName.isEmpty()){
            name.setError(res.getString(R.string.profile_edit_wrong_fields));
        }
        if(editSurname.isEmpty()){
            surname.setError(res.getString(R.string.profile_edit_wrong_fields));
        }
        if(editEmail.isEmpty()){
            email.setError(res.getString(R.string.profile_edit_wrong_fields));
        }
        if(editPhone.isEmpty()){
            phone.setError(res.getString(R.string.profile_edit_wrong_fields));
        }

        ProfileEditRequest profileEditRequest = new ProfileEditRequest(editName,editSurname,editEmail,editPhone);

        if(authUtil.getRole().equals(Role.ADMIN.name())){
            loadingData.setEnableProgressBar();

            AdminService adminService = ApiClient.getAdminService();
            Call<Customer> call = adminService.updateProfile(authUtil.getToken(), profileEditRequest);
            call.enqueue(new Callback<Customer>() {
                @Override
                public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {
                    if(!response.isSuccessful()){
                        if(response.code() == 403){
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                            Toast.makeText(requireActivity(),res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(response.errorBody() != null) {
                                try {
                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                    if(message.equals(USER_NOT_FOUND))
                                        Toast.makeText(requireActivity(),res.getString(R.string.user_not_found),Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        loadingData.setDisableProgressBar();
                    }
                    else{
                        Customer customer = response.body();
                        if(customer != null){
                            Toast.makeText(requireActivity(),res.getString(R.string.profile_edit_success),Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Customer> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }
        else{
            CustomerService customerService = ApiClient.getCustomerService();
            loadingData.setEnableProgressBar();

            Call<Customer> call = customerService.updateProfile(authUtil.getToken(),profileEditRequest);
            call.enqueue(new Callback<Customer>() {
                @Override
                public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {
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
                        Customer customer = response.body();
                        if(customer != null){
                            Toast.makeText(requireActivity(),res.getString(R.string.profile_edit_success) ,Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Customer> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }

    }
}
