package com.example.tilldock.data.api;

import com.example.tilldock.data.model.AuthResponse;
import com.example.tilldock.data.model.LoginRequest;
import com.example.tilldock.data.model.MerchantResponse;
import com.example.tilldock.data.model.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body SignupRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<Void> logout();

    @GET("api/auth/me")
    Call<MerchantResponse> me();
}
