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

    // --- AUTH ---
    @POST("auth/register.php")
    Call<JsonObject> registerUser(@Body JsonObject userData);

    @POST("auth/login.php")
    Call<JsonObject> loginUser(@Body JsonObject credentials);

    // --- SYNC ---
    @GET("sync/references.php")
    Call<JsonObject> getCarbonReferences();

    @POST("sync/report_packages.php")
    Call<Void> reportUnknownPackages(@Body List<String> packageNames);

    @POST("sync/upload_logs.php")
    Call<JsonObject> uploadDailyLogs(@Header("Authorization") String token, @Body JsonObject dailyData);

    // --- MARKETPLACE ---
    @GET("marketplace/goals.php")
    Call<JsonObject> getMarketplaceGoals();

    @POST("marketplace/contribute.php")
    Call<JsonObject> contributeToGoal(@Header("Authorization") String token, @Body JsonObject contributionData);

    // --- SOCIAL ---
    @GET("community/streaks.php")
    Call<JsonObject> getFriendStreaks(@Header("Authorization") String token);

    // --- CONTENT ---
    @GET("content/news.php")
    Call<JsonObject> getBlogPosts();

    // Legacy support
    @GET("tree-models")
    Call<TreeResponse> getTrees(@Header("evertreen-user-apikey") String apiKey);
}