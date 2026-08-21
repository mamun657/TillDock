package com.example.tilldock.data.api;

import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.ProductRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApi {

    @GET("api/products")
    Call<List<Product>> list(
            @Query("includeArchived") Boolean includeArchived,
            @Query("query") String query,
            @Query("categoryId") String categoryId,
            @Query("status") String status,
            @Query("sort") String sort
    );

    @GET("api/products/{id}")
    Call<Product> get(@Path("id") String id);

    @POST("api/products")
    Call<Product> create(@Body ProductRequest request);

    @PUT("api/products/{id}")
    Call<Product> update(@Path("id") String id, @Body ProductRequest request);

    @POST("api/products/{id}/archive")
    Call<Product> archive(@Path("id") String id);

    @POST("api/products/{id}/restore")
    Call<Product> restore(@Path("id") String id);

    @Multipart
    @POST("api/products/{id}/image")
    Call<Product> uploadImage(@Path("id") String id, @Part MultipartBody.Part file);

    @DELETE("api/products/{id}/image")
    Call<Product> deleteImage(@Path("id") String id);

    @PUT("api/products/{id}/image-url")
    Call<Product> setImageUrl(@Path("id") String id, @Body RequestBody body);
}