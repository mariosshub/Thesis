package com.example.gymapplication.Customer.Fragments;

import static com.example.gymapplication.Api.ApiExceptions.RESERVATION_NOT_FOUND;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapplication.Api.ApiClient;
import com.example.gymapplication.Api.ApiError;
import com.example.gymapplication.Api.ApiExceptions;
import com.example.gymapplication.Utils.AuthUtil;
import com.example.gymapplication.Customer.Adapters.ShowAllReservationsAdapter;
import com.example.gymapplication.R;
import com.example.gymapplication.Response.ReservationsResponse;
import com.example.gymapplication.Service.CustomerService;
import com.example.gymapplication.Utils.JwtUtil;
import com.example.gymapplication.Utils.LoadingData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowAllReservationsFragment extends Fragment {
    private List<ReservationsResponse> reservationsResponseList = new ArrayList<>();
    private boolean firstTimeItemSelected = true;
    private Resources res;
    private LoadingData loadingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_all_reservations_fragment,container,false);
        res = requireActivity().getResources();

        Spinner showReservationsSpinner = view.findViewById(R.id.reservationsToShowSpinner);
        RelativeLayout parent = view.findViewById(R.id.show_all_reservations);

        loadingData = new LoadingData(requireActivity());
        loadingData.setProgressBarForRelLayout(parent);

        RecyclerView allReservationsRecView = view.findViewById(R.id.allReservationsRecView);
        ShowAllReservationsAdapter showAllReservationsAdapter = new ShowAllReservationsAdapter(requireActivity(), loadingData);
        allReservationsRecView.setAdapter(showAllReservationsAdapter);
        allReservationsRecView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        setReservations(showAllReservationsAdapter);

        String[] reservations = new String[]{res.getString(R.string.reservations_all),res.getString(R.string.reservations_current)};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, reservations);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        showReservationsSpinner.setAdapter(arrayAdapter);

        showReservationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // check when fragment loads for the first time
                if(!firstTimeItemSelected){
                    // user has selected to show all reservations
                    if(position == 0){
                        showAllReservationsAdapter.getFilter().filter("");
                    }
                    //user has selected to show only the current reservations
                    else{
                        showAllReservationsAdapter.getFilter().filter("currentRes");
                    }
                }
                else{
                    firstTimeItemSelected = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void setReservations(ShowAllReservationsAdapter showAllReservationsAdapter){
        CustomerService customerService = ApiClient.getCustomerService();
        AuthUtil authUtil = new AuthUtil(requireActivity());

        loadingData.setEnableProgressBar();

        Call<List<ReservationsResponse>> call = customerService.getAllReservations(authUtil.getToken());
        call.enqueue(new Callback<List<ReservationsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReservationsResponse>> call, @NonNull Response<List<ReservationsResponse>> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 403){
                        new JwtUtil(authUtil).refreshJwt(authUtil.getUsername(),authUtil.getRefreshToken());
                        Toast.makeText(requireActivity(), res.getString(R.string.auth_renewed), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(response.errorBody() != null) {
                            try {
                                String message = new Gson().fromJson(response.errorBody().charStream(), ApiError.class).getMessage();
                                if(message.equals(RESERVATION_NOT_FOUND))
                                    Toast.makeText(requireActivity(),res.getString(R.string.res_not_found),Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    loadingData.setDisableProgressBar();
                }
                else{
                    if(response.body() != null){
                        reservationsResponseList = response.body();
                        showAllReservationsAdapter.setReservationsList(reservationsResponseList);
                        loadingData.setDisableProgressBar();
                        // leave empty so that it shows all reservations
                        showAllReservationsAdapter.getFilter().filter("");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReservationsResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireActivity(),t.getMessage(),Toast.LENGTH_SHORT).show();
                loadingData.setDisableProgressBar();
            }
        });
    }

}
