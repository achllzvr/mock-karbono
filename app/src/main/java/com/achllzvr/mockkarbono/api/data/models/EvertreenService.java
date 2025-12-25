package com.achllzvr.mockkarbono.api.data.models;

import com.achllzvr.mockkarbono.api.data.models.TreeResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface EvertreenService {
    // The endpoint defined in YAML is /tree-models
    @GET("tree-models")
    Call<TreeResponse> getTrees(
            @Header("evertreen-user-apikey") String apiKey
    );
}