package com.example.tilldock.data.api;

import com.example.tilldock.data.model.Business;
import com.example.tilldock.data.model.BusinessRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface BusinessApi {

    @GET("api/business")
    Call<Business> get();

    @PUT("api/business")
    Call<Business> upsert(@Body BusinessRequest request);

    @DELETE("api/business")
    Call<Void> delete();
}