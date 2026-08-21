package com.example.tilldock.data.api;

import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.CategoryRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryApi {

    @GET("api/categories")
    Call<List<Category>> list();

    @POST("api/categories")
    Call<Category> create(@Body CategoryRequest request);

    @PUT("api/categories/{id}")
    Call<Category> update(@Path("id") String id, @Body CategoryRequest request);

    @DELETE("api/categories/{id}")
    Call<Void> delete(@Path("id") String id);
}