// FinancialNewsApi.java
package com.example.myapplication.network;

import com.example.myapplication.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FinancialNewsApi {
    @GET("v2/top-headlines")
    Call<NewsResponse> getFinancialNews(@Query("category") String category, @Query("apiKey") String apiKey);

    @GET("v2/everything")
    Call<NewsResponse> getEconomicNews(@Query("q") String query, @Query("language") String language, @Query("apiKey") String apiKey);
}