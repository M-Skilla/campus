package com.group.campus.utils;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AlgoliaApi {
    @Headers({
            "X-Algolia-Application-Id: S1DZ3TXW58",       // Replace with your App ID
            "X-Algolia-API-Key: b32ff28687339e5bb9b0ad1914d0e987", // Search-only key
            "Content-Type: application/json"
    })
    @POST("/1/indexes/{indexName}/query")
    Call<JsonObject> search(
            @Path("indexName") String indexName,
            @Body JsonObject body
    );
}
