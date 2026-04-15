package com.gpsapp.client.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static GpsApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.194:8080/")
//            retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.201:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GpsApiService.class);
    }
}
