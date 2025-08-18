package com.group.campus.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://ahbzglojvzgsxdmnpwza.supabase.co";

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFoYnpnbG9qdnpnc3hkbW5wd3phIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTUxNzUzNjcsImV4cCI6MjA3MDc1MTM2N30.TX5bTffy2llh8UFMHLuwfaACWDcQGIdOxTff-TofC7U";

    private Retrofit retrofit;

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("apikey", API_KEY)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
