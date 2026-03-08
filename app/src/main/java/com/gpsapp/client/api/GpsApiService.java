package com.gpsapp.client.api;

import com.gpsapp.client.dto.LocationDto;
import com.gpsapp.client.model.LoginRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GpsApiService {

    @POST("/api/auth/login")
    Call<ResponseBody> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/locations")
    Call<ResponseBody> saveLocation(@Body LocationDto locationDto, @Header("Authorization") String token);

    @GET("/api/locations")
    Call<List<LocationDto>> getAllLocations(@Header("Authorization") String token);
}
