package com.example.gymapplication.Service;

import com.example.gymapplication.Model.Subscription;
import com.example.gymapplication.Request.CountForReservationsRequest;
import com.example.gymapplication.Request.MakeReservationRequest;
import com.example.gymapplication.Request.ProfileEditRequest;
import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Response.AvailabilityForResResponse;
import com.example.gymapplication.Response.ReservationsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CustomerService {
    @GET("customer/showProfile")
    Call<Customer> showProfile (@Header("Authorization") String token);

    @PUT("customer/updateProfile")
    Call<Customer> updateProfile(@Header("Authorization") String token, @Body ProfileEditRequest profileEditRequest);

    @PUT("customer/updatePhoto/{imgUrl}")
    Call<Boolean> updatePhoto(@Header("Authorization")String token, @Path("imgUrl") String imgUrl);

    @GET("customer/findAllAvailabilities")
    Call<List<AvailabilityForResResponse>> getAllAvailabilities (@Header("Authorization") String token);

    @POST("customer/makeReservation")
    Call<Boolean> makeReservation (@Header("Authorization") String token, @Body MakeReservationRequest reservationRequest);

    @GET("customer/findAllReservations")
    Call<List<ReservationsResponse>> getAllReservations (@Header("Authorization") String token);

    @POST("customer/countReservations")
    Call<Long> countReservations (@Header("Authorization") String token, @Body CountForReservationsRequest countForReservationsRequest);

    @PUT("customer/cancelReservation/{id}")
    Call<Boolean> cancelReservation (@Header("Authorization") String token, @Path("id") Long id);

    @GET("customer/findSub")
    Call<Subscription> getSub (@Header("Authorization") String token);

    @PUT("customer/checkSubExpiration")
    Call<Boolean> checkSubExpiration (@Header("Authorization") String token);
}
