package com.group.campus.service;

import com.group.campus.dtos.LoginRequest;
import com.group.campus.dtos.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {

    @POST("/functions/v1/login-with-reg")
    Call<LoginResponse> login(@Body LoginRequest body);
}
