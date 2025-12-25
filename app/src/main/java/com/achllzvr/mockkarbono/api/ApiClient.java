package com.achllzvr.mockkarbono.api;

import com.achllzvr.mockkarbono.api.data.models.EvertreenService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Verified Base URL from Evertreen Documentation
    private static final String BASE_URL = "https://www.evertreen.com/api/";
    private static Retrofit retrofit = null;

    public static EvertreenService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(EvertreenService.class);
    }
}