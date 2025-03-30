// ExchangeRateApi.java
package com.example.myapplication.network;

import com.example.myapplication.models.ExchangeRateResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ExchangeRateApi {
    @GET("latest")
    Call<ExchangeRateResponse> getExchangeRates(@Query("base") String base);
}