package com.group.campus.utils;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlgoliaClient {

        private static final String BASE_URL = "https://S1DZ3TXW58-dsn.algolia.net"; // Replace YOUR_APP_ID
        private static Retrofit retrofit;

        public static AlgoliaApi getService() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit.create(AlgoliaApi.class);
        }

}
