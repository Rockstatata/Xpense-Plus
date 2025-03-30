// Discover.java
package com.example.myapplication.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.myapplication.R;
import com.example.myapplication.models.NewsResponse;
import com.example.myapplication.network.FinancialNewsApi;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.adapters.DiscoverAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class Discover extends Fragment {

    private RecyclerView recyclerView1, recyclerView2;
    private DiscoverAdapter adapter1, adapter2;
    private List<NewsResponse.Article> articles1 = new ArrayList<>(), articles2 = new ArrayList<>();
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/46fdb761ea8a4d03fb16de9f/latest/BDT";
    private TextView tvUSD, tvEUR, tvGBP;
    private RequestQueue requestQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        recyclerView1 = view.findViewById(R.id.recyclerViewFinancialNews);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter1 = new DiscoverAdapter(articles1);
        recyclerView1.setAdapter(adapter1);

        recyclerView2 = view.findViewById(R.id.recyclerViewEconomicNews);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter2 = new DiscoverAdapter(articles2);
        recyclerView2.setAdapter(adapter2);

        tvUSD = view.findViewById(R.id.tvUSD);
        tvEUR = view.findViewById(R.id.tvEUR);
        tvGBP = view.findViewById(R.id.tvGBP);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());

        fetchExchangeRates();
        fetchFinancialNews();
        fetchEconomicInfo();

        return view;
    }

    private void fetchExchangeRates() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse JSON response
                            JSONObject conversionRates = response.getJSONObject("conversion_rates");

                            // Extract rates for important currencies
                            double rateUSD = conversionRates.getDouble("USD");
                            double rateEUR = conversionRates.getDouble("EUR");
                            double rateGBP = conversionRates.getDouble("GBP");

                            // Display rates in the TextViews
                            tvUSD.setText("USD: " + rateUSD);
                            tvEUR.setText("EUR: " + rateEUR);
                            tvGBP.setText("GBP: " + rateGBP);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ExchangeRate", "JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ExchangeRate", "Error fetching exchange rates: " + error.getMessage());
                    }
                }
        );

        // Add the request to the Volley RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void fetchFinancialNews() {
        FinancialNewsApi api = RetrofitClient.getClient("https://newsapi.org/").create(FinancialNewsApi.class);
        Call<NewsResponse> call = api.getFinancialNews("business", "a2184a3a572a4d6b85119f8ace8e63f2");
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, retrofit2.Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    articles1.addAll(response.body().getArticles());
                    adapter1.notifyDataSetChanged();
                } else {
                    Log.e("Discover", "Response not successful: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("Discover", "Failed to fetch financial news", t);
            }
        });
    }

    private void fetchEconomicInfo() {
        FinancialNewsApi api = RetrofitClient.getClient("https://newsapi.org/").create(FinancialNewsApi.class);
        Call<NewsResponse> call = api.getEconomicNews("Bangladesh economy", "en", "a2184a3a572a4d6b85119f8ace8e63f2");
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, retrofit2.Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    articles2.addAll(response.body().getArticles());
                    adapter2.notifyDataSetChanged();
                } else {
                    Log.e("Discover", "Response not successful: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("Discover", "Failed to fetch economic news", t);
            }
        });
    }
}