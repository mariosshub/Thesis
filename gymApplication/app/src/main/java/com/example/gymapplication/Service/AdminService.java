package com.example.gymapplication.Service;

import com.example.gymapplication.Model.Customer;
import com.example.gymapplication.Model.Subscription;
import com.example.gymapplication.Model.WorkoutLessons;
import com.example.gymapplication.Request.AddSubRequest;
import com.example.gymapplication.Request.EditSubRequest;
import com.example.gymapplication.Request.LessonAvailabilityRequest;
import com.example.gymapplication.Request.ProfileEditRequest;
import com.example.gymapplication.Request.WorkoutLessonRequest;
import com.example.gymapplication.Response.SubStatisticsResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminService {
    @GET("admin/showProfile")
    Call<Customer> showProfile(@Header("Authorization") String token);

    @PUT("admin/updateProfile")
    Call<Customer> updateProfile(@Header("Authorization") String token, @Body ProfileEditRequest profileEditRequest);

    @PUT("admin/updatePhoto/{imgUrl}")
    Call<Boolean> updatePhoto(@Header("Authorization")String token, @Path("imgUrl") String imgUrl);

    @GET("admin/findAllLessons")
    Call<List<WorkoutLessons>> getAllLessons(@Header("Authorization") String token);

    @GET("admin/checkLessonsAvailabilitiesIfEmpty")
    Call<Boolean> checkLessonsAvailabilitiesIfEmpty(@Header("Authorization") String token);

    @POST("admin/addLesson")
    Call<WorkoutLessons> addWorkoutLesson(@Header("Authorization") String token, @Body WorkoutLessonRequest workoutLessonRequest);

    @PUT("admin/updateLesson")
    Call<WorkoutLessons> editWorkoutLesson(@Header("Authorization") String token, @Body WorkoutLessonRequest workoutLessonRequest);

    @POST("admin/addLessonAvailability")
    Call<WorkoutLessons> addLessonAvailability(@Header("Authorization") String token, @Body LessonAvailabilityRequest lessonAvailabilityRequest);

    @DELETE("admin/deleteWorkoutLessonAvailability/{id}")
    Call<WorkoutLessons> deleteLessonAvailability(@Header("Authorization") String token, @Path("id") long id);

    @GET("admin/findSubByEmail/{email}")
    Call<Subscription> findSubByEmail(@Header("Authorization") String token, @Path("email") String email);

    @POST("admin/addSubToCustomer")
    Call<Subscription> addSubToCustomer(@Header("Authorization") String token, @Body AddSubRequest addSubRequest);

    @PUT("admin/updateSub")
    Call<Subscription> updateSub(@Header("Authorization")String token, @Body EditSubRequest editSubRequest);

    @GET("admin/getMostSubLessons")
    Call<SubStatisticsResponse> getMostSubLessons(@Header("Authorization") String token);

    @GET("admin/getSumLessonCost")
    Call<Map<String,Double>> getSumLessonCost(@Header("Authorization") String token);

    @GET("admin/getMostPopularLessonsByDate/{day}")
    Call<Map<String,Long>> getMostPopularLessonsByDate(@Header("Authorization") String token, @Path("day") String day);

    @GET("admin/getMostPopularLessonsByMonth/{year}/{month}")
    Call<Map<String,Long>> getMostPopularLessonsByMonth(@Header("Authorization") String token, @Path("year") int year, @Path("month") int month);

    @GET("admin/getPopularHoursOfDay/{day}")
    Call<Map<String,Long>> getPopularHoursOfDay(@Header("Authorization")String token, @Path("day")String day);
}
