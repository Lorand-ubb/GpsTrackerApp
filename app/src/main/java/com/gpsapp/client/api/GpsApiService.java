package com.gpsapp.client.api;

import com.gpsapp.client.model.LoginRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GpsApiService {

    @POST("/api/auth/login")
    Call<ResponseBody> loginUser(@Body LoginRequest loginRequest);
}
