package com.achllzvr.mockkarbono.api;

import com.achllzvr.mockkarbono.api.data.models.MarketplaceGoal;
import com.achllzvr.mockkarbono.api.data.models.TreeResponse;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface KarbonoApiService {

    // --- AUTHENTICATION ---

    @POST("auth/register.php")
    Call<JsonObject> registerUser(@Body JsonObject userData);

    @POST("auth/login.php")
    Call<JsonObject> loginUser(@Body JsonObject credentials);


    // --- SYNC & LOGIC ---

    // 1. Download the latest "Carbon Math" (The table you just made)
    @GET("sync/references.php")
    Call<JsonObject> getCarbonReferences();

    // 2. Upload "Unknown Apps" found on the phone
    @POST("sync/report_packages.php")
    Call<Void> reportUnknownPackages(@Body List<String> packageNames);

    // 3. Sync Daily Logs
    @POST("sync/upload_logs.php")
    Call<JsonObject> uploadDailyLogs(
            @Header("Authorization") String token,
            @Body JsonObject dailyData
    );


    // --- MARKETPLACE & SOCIAL ---

    // This is the method causing your error!
    @GET("marketplace/goals.php")
    Call<JsonObject> getMarketplaceGoals(); // Returns list of goals

    @POST("marketplace/contribute.php")
    Call<JsonObject> contributeToGoal(
            @Header("Authorization") String token,
            @Body JsonObject contributionData
    );

    @GET("community/streaks.php")
    Call<JsonObject> getFriendStreaks(@Header("Authorization") String token);

    // Legacy support (optional, if you still reference it somewhere)
    @GET("tree-models")
    Call<TreeResponse> getTrees(@Header("evertreen-user-apikey") String apiKey);
}