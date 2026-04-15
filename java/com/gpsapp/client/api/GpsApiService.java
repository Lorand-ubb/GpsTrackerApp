package com.gpsapp.client.api;

import com.gpsapp.client.dto.LocationDto;
import com.gpsapp.client.dto.RegisterRequest;
import com.gpsapp.client.dto.UserDTO;
import com.gpsapp.client.model.LoginRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface GpsApiService {

    @POST("/api/auth/login")
    Call<ResponseBody> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/auth/register")
    Call<ResponseBody> registerUser(@Body RegisterRequest registerRequest);

    @POST("/api/locations")
    Call<ResponseBody> saveLocation(@Body LocationDto locationDto, @Header("Authorization") String token);

    @GET("/api/locations")
    Call<List<LocationDto>> getAllLocations(@Header("Authorization") String token);

    @DELETE("/api/locations")
    Call<ResponseBody> deleteLocation(@Query("latitude") double latitude, @Query("longitude") double longitude, @Header("Authorization") String token);

    @GET("api/auth/me")
    Call<UserDTO> getMe(@Header("Authorization") String token);
    @PUT("api/auth/me")
    Call<ResponseBody> updateMe(@Header("Authorization") String token, @Body UserDTO userDTO);
}
