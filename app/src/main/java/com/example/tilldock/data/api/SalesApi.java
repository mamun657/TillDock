package com.example.tilldock.data.api;

import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.model.SaleReport;
import com.example.tilldock.data.model.SaleRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SalesApi {

    @POST("api/sales")
    Call<Sale> create(@Body SaleRequest request);

    @GET("api/sales")
    Call<List<Sale>> list(@Query("businessId") String businessId);

    @GET("api/sales/{id}")
    Call<Sale> get(@Path("id") String id);

    @GET("api/sales/reports")
    Call<SaleReport> report(@Query("period") String period);
}