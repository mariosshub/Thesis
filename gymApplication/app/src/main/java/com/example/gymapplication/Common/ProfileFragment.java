package com.example.gymapplication.Common;

import static com.example.gymapplication.Api.ApiExceptions.USER_NOT_FOUND;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Model.Role;
import com.example.gymapplication.R;
import com.example.gymapplication.Service.AdminService;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private TextView name, surname, email, phone, editProfile, editPhoto, savePhoto;
    private ImageView profileImage;
    private Customer user;
    private Resources res;
    private ConstraintLayout parent;

    private AuthUtil authUtil;
    private StorageReference storageReference;
    private Uri filePath;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        res = requireActivity().getResources();

        initViews(view);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        editor = sharedPreferences.edit();

        // get the URI where the image is stored
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the returned Uri
                    editor.putBoolean("editPhoto",true);
                    editor.apply();
                    filePath = uri;
                    profileImage.setImageURI(filePath);
                    editPhoto.setVisibility(View.GONE);
                    savePhoto.setVisibility(View.VISIBLE);
                });

        // Create a FirebaseStorage reference
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePics");

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForConLayout(parent);

        editPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        editProfile.setOnClickListener(this);
        savePhoto.setOnClickListener(this);


        authUtil = new AuthUtil(requireActivity());

        // check if users role is admin
        if(authUtil.getRole().equals(Role.ADMIN.name())){
            AdminService adminService = ApiClient.getAdminService();
            loadingData.setEnableProgressBar();

            //show admins profile
            Call<Customer> call = adminService.showProfile(authUtil.getToken());
            call.enqueue(new Callback<Customer>() {
                @Override
                public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {
                    if(!response.isSuccessful()){
                        if(response.code() == 403){
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                            Toast.makeText(getActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
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
                    }
                    else{
                        if(response.body() != null){
                            user = response.body();
                            setCustomerFields();
                            loadingData.setDisableProgressBar();
                            loadProfileImage(user.getImgUrl());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Customer> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }
        else{
            CustomerService customerService = ApiClient.getCustomerService();
            loadingData.setEnableProgressBar();

            // show customers profile
            Call<Customer> call = customerService.showProfile(authUtil.getToken());
            call.enqueue(new Callback<Customer>() {
                @Override
                public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {
                    if(!response.isSuccessful()){
                        if(response.code() == 403){
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                            Toast.makeText(getActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
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
                    }
                    else{
                        if(response.body() != null){
                            user = response.body();
                            setCustomerFields();
                            loadingData.setDisableProgressBar();
                            loadProfileImage(user.getImgUrl());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Customer> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        // button for editing profile
        if (v.getId() == R.id.textViewEditFields) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("editUser",user);
            EditProfileFragment fragment = new EditProfileFragment();
            fragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
        // button for saving photo
        else if(v.getId() == R.id.textViewSavePhoto){
            saveEditedPhoto();
        }
    }

    private void loadProfileImage(String imgUrl){
        loadingData.setEnableProgressBar();
        editPhoto.setVisibility(View.GONE);
        try {
            File file = File.createTempFile("temp",".jpg");
            storageReference
                    .child(imgUrl)
                    .getFile(file)
                    .addOnSuccessListener(s -> {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        profileImage.setImageBitmap(bitmap);

                        // set the profile image to the navigation view
                        try{
                            NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
                            ImageView profilePic = navigationView.getHeaderView(0).findViewById(R.id.profile_pic);
                            profilePic.setImageBitmap(bitmap);
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                        editPhoto.setVisibility(View.VISIBLE);
                        loadingData.setDisableProgressBar();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), res.getString(R.string.image_failed), Toast.LENGTH_SHORT).show();
                        editPhoto.setVisibility(View.VISIBLE);
                        loadingData.setDisableProgressBar();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePhotoToDB(String imgUrl){
        if(authUtil.getRole().equals(Role.ADMIN.name())) {

            AdminService adminService = ApiClient.getAdminService();
            Call<Boolean> call = adminService.updatePhoto(authUtil.getToken(),imgUrl);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 403) {
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(), authUtil.getRefreshToken());
                            Toast.makeText(getActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        loadingData.setDisableProgressBar();
                    } else {
                        if (response.body() != null && response.body()) {
                            Toast.makeText(getActivity(), res.getString(R.string.image_success), Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }
        else if(authUtil.getRole().equals(Role.CUSTOMER.name())){

            CustomerService customerService = ApiClient.getCustomerService();
            Call<Boolean> call = customerService.updatePhoto(authUtil.getToken(),imgUrl);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 403) {
                            new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(), authUtil.getRefreshToken());
                            Toast.makeText(getActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        loadingData.setDisableProgressBar();
                    } else {
                        if (response.body() != null && response.body()) {
                            Toast.makeText(getActivity(), res.getString(R.string.image_success), Toast.LENGTH_SHORT).show();
                            loadingData.setDisableProgressBar();
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingData.setDisableProgressBar();
                }
            });
        }
    }
    private void saveEditedPhoto(){
        if (filePath != null) {
            loadingData.setEnableProgressBar();

            // adding a UUID as the name of the image in order to act as a unique key
            String randomKeyImage;
            if(user.getImgUrl().equals("default_profile.png")){
                randomKeyImage = UUID.randomUUID().toString() + ".png";
            }
            else{
                randomKeyImage = user.getImgUrl();
            }

            //saving image to firebase storage
            storageReference
                    .child(randomKeyImage)
                    .putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        editPhoto.setVisibility(View.VISIBLE);
                        savePhoto.setVisibility(View.GONE);
                        // save image url to db
                        savePhotoToDB(randomKeyImage);
                    })
                    .addOnFailureListener(e -> {
                        editPhoto.setVisibility(View.VISIBLE);
                        savePhoto.setVisibility(View.GONE);
                        Toast.makeText(requireActivity(), res.getString(R.string.image_upload_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingData.setDisableProgressBar();
                    });
        }
    }

    private void setCustomerFields(){
        name.setText(user.getName());
        surname.setText(user.getSurname());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
    }

    private void initViews(View view){
        parent = view.findViewById(R.id.profile_fragment);
        name = view.findViewById(R.id.textViewName);
        surname = view.findViewById(R.id.textViewSurname);
        email = view.findViewById(R.id.textViewEmail);
        phone = view.findViewById(R.id.textViewPhone);
        editProfile = view.findViewById(R.id.textViewEditFields);
        editPhoto = view.findViewById(R.id.textViewEditPhoto);
        savePhoto = view.findViewById(R.id.textViewSavePhoto);
        profileImage = view.findViewById(R.id.imageViewProfile);
    }
}
